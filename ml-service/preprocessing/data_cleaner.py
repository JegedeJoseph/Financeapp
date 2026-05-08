import re
import string

def clean_description(text: str) -> str:
    """Clean and normalize transaction description"""
    if not text:
        return ""

    # Convert to lowercase
    text = text.lower()

    # Remove special characters except spaces
    text = re.sub(r'[^\w\s]', ' ', text)

    # Remove extra whitespace
    text = ' '.join(text.split())

    # Common abbreviations normalization
    replacements = {
        'pos': '',
        'atm': '',
        'm tn': 'mtn',
        'j umia': 'jumia',
    }

    for old, new in replacements.items():
        text = text.replace(old, new)

    # Remove common stop words that don't help categorization
    stop_words = {'payment', 'purchase', 'transaction', 'debit', 'credit', 'transfer'}
    words = [w for w in text.split() if w not in stop_words]

    return ' '.join(words)