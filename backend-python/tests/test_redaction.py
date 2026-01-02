import os
import tempfile
import fitz
from app import redact_pdf

def create_pdf_with_text(path, text):
    doc = fitz.open()
    page = doc.new_page()
    page.insert_text((72,72), text)
    doc.save(path)
    doc.close()

def test_redact_email(tmp_path):
    src = os.path.join(tmp_path, "src.pdf")
    dst = os.path.join(tmp_path, "dst.pdf")
    create_pdf_with_text(src, "Contact: alice@example.com")
    count = redact_pdf(src, dst, [r"[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+"])
    assert count >= 1
    assert os.path.exists(dst)
