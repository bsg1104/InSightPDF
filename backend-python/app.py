import os
import io
import re
import uuid
import time
from flask import Flask, request, jsonify, send_file
import fitz  # PyMuPDF
from werkzeug.utils import secure_filename

app = Flask(__name__)
UPLOAD_DIR = "/data/uploads"
OUTPUT_DIR = "/data/outputs"
os.makedirs(UPLOAD_DIR, exist_ok=True)
os.makedirs(OUTPUT_DIR, exist_ok=True)

DEFAULT_PATTERNS = [
    r"[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+",  # emails
    r"\b\d{3}-\d{2}-\d{4}\b"  # SSN
]

def extract_structured_text(doc):
    pages = []
    for page in doc:
        text = page.get_text("text")
        # simple paragraph split
        paragraphs = [p.strip() for p in text.split("\n\n") if p.strip()]
        pages.append({"page": page.number + 1, "paragraphs": paragraphs})
    return pages

def redact_pdf(src_path, dst_path, patterns):
    doc = fitz.open(src_path)
    redacted_count = 0
    compiled = [re.compile(p) for p in patterns]
    for page in doc:
        page_text = page.get_text("text")
        for pat in compiled:
            for m in pat.finditer(page_text):
                match_text = m.group(0)
                # find occurrences on page and redact
                areas = page.search_for(match_text)
                for r in areas:
                    page.add_redact_annot(r, fill=(0,0,0))
                    redacted_count += 1
        # apply per-page after annotations
    if redacted_count:
        doc.apply_redactions()
    doc.save(dst_path)
    return redacted_count

@app.route("/process", methods=["POST"])
def process():
    start = time.time()
    if "file" in request.files:
        f = request.files["file"]
        filename = secure_filename(f.filename)
        uid = str(uuid.uuid4())
        src_path = os.path.join(UPLOAD_DIR, f"{uid}_{filename}")
        f.save(src_path)
    else:
        return jsonify({"error": "no file provided"}), 400

    custom = request.form.get("patterns", "")
    extra = [p for p in custom.split(";") if p.strip()]
    patterns = DEFAULT_PATTERNS + extra

    doc = fitz.open(src_path)
    structured = extract_structured_text(doc)
    out_path = os.path.join(OUTPUT_DIR, f"redacted_{os.path.basename(src_path)}")
    redacted_count = redact_pdf(src_path, out_path, patterns)
    elapsed = time.time() - start

    response = {
        "id": os.path.basename(out_path),
        "input": src_path,
        "output": out_path,
        "redacted_count": redacted_count,
        "processing_time": elapsed,
        "structured": structured
    }
    return jsonify(response), 200

@app.route("/download/<filename>", methods=["GET"])
def download(filename):
    path = os.path.join(OUTPUT_DIR, filename)
    if not os.path.exists(path):
        return jsonify({"error": "not found"}), 404
    return send_file(path, as_attachment=True)
    
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
