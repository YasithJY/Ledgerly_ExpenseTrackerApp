from reportlab.lib.pagesizes import A4
from reportlab.pdfgen import canvas
import sys

def create_receipts_pdf(filename):
    c = canvas.Canvas(filename, pagesize=A4)
    width, height = A4
    
    # Text data for the receipts
    receipts = [
        [
            "KEELLS SUPER", "123 Main Street", "Colombo 03", "-----------------------",
            "Milk        Rs 450.00", "Bread       Rs 150.00", "Eggs        Rs 300.00",
            "-----------------------", "TOTAL       Rs 900.00", "-----------------------",
            "Thank you for shopping!"
        ],
        [
            "CAFE COLOMBO", "Table: 12", "Server: John", "-----------------------",
            "Latte          850.00", "Sandwich     1,200.00", "-----------------------",
            "Subtotal     2,050.00", "Service(10%)   205.00", "-----------------------",
            "Amount Due LKR 2,255.00", "Please come again"
        ],
        [
            "TECH GADGETS", "Order #99281", "-----------------------",
            "Wireless Mouse  15,400.00", "USB-C Cable      2,500.00", "-----------------------",
            "ITEMS: 2", "", "             17,900.00", "-----------------------",
            "Returns within 14 days"
        ],
        [
            "READERS HUB", "TAX INVOICE", "-----------------------",
            "Fiction Novel   $ 14.99", "Notebook         $ 5.50", "-----------------------",
            "Total           $ 20.49", "Keep this invoice for returns"
        ],
        [
            "FUEL STATION", "Pump #4", "-----------------------",
            "Petrol 95", "Volume (L)        10.50", "Rate/L           390.00",
            "-----------------------", "TOTAL          4,095.00", "Drive Safely!"
        ],
        [
            "LONDON BREW", "Oxford Street", "-----------------------",
            "Espresso         £ 2.50", "Muffin           £ 3.20", "-----------------------",
            "Total Paid       £ 5.70", "VAT #12345678"
        ]
    ]

    c.setFont("Courier", 12)
    
    # 3x2 grid layout
    cols = 3
    rows = 2
    x_margins = [30, 230, 430]
    y_margins = [height - 80, height - 450]
    
    for i, receipt in enumerate(receipts):
        col = i % cols
        row = i // cols
        
        x = x_margins[col]
        y = y_margins[row]
        
        for line in receipt:
            c.drawString(x, y, line)
            y -= 18
            
    c.save()

if __name__ == "__main__":
    create_receipts_pdf("sample_receipts.pdf")
