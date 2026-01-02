from reportlab.lib.pagesizes import letter
from reportlab.pdfgen import canvas
import os
os.makedirs("../data/sample_pdfs", exist_ok=True)
path = "../data/sample_pdfs/sample1.pdf"
c = canvas.Canvas(path, pagesize=letter)
c.drawString(72, 720, "InSIGHTPDF Sample Document")
c.drawString(72, 700, "Contact: alice@example.com")
c.drawString(72, 680, "SSN: 123-45-6789")
c.save()
print("Generated", path)
