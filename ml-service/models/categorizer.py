from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.naive_bayes import MultinomialNB
from sklearn.pipeline import Pipeline
import numpy as np
import logging

logger = logging.getLogger(__name__)

class TransactionCategorizer:
    """Multinomial Naive Bayes for transaction categorization"""

    def __init__(self):
        # Training data - common Nigerian transaction descriptions
        self.training_data = [
            # Groceries
            ("shoprite ikeja", "GROCERIES"),
            ("spar lekki", "GROCERIES"),
            ("market basket", "GROCERIES"),
            ("grocery store", "GROCERIES"),
            ("foodco ibadan", "GROCERIES"),
            ("justrite supermarket", "GROCERIES"),
            ("hubmart lekki", "GROCERIES"),
            ("freshforte abuja", "GROCERIES"),

            # Dining Out
            ("restaurant payment", "DINING_OUT"),
            ("mcdonalds drive", "DINING_OUT"),
            ("chicken republic", "DINING_OUT"),
            ("dominos pizza", "DINING_OUT"),
            ("kfc abuja", "DINING_OUT"),
            ("the place lagos", "DINING_OUT"),
            ("mr biggs", "DINING_OUT"),
            ("cold stone creamery", "DINING_OUT"),

            # Transportation
            ("uber trip", "TRANSPORTATION"),
            ("bolt ride", "TRANSPORTATION"),
            ("lagride payment", "TRANSPORTATION"),
            ("fuel station", "TRANSPORTATION"),
            ("total petrol", "TRANSPORTATION"),
            ("oando filling", "TRANSPORTATION"),
            ("conoil station", "TRANSPORTATION"),
            ("bus fare", "TRANSPORTATION"),
            ("okada ride", "TRANSPORTATION"),

            # Utilities
            ("m tn data", "UTILITIES"),
            ("airtel recharge", "UTILITIES"),
            ("glo subscription", "UTILITIES"),
            ("9mobile topup", "UTILITIES"),
            ("phed electricity", "UTILITIES"),
            ("ekedc bill", "UTILITIES"),
            ("ikedc payment", "UTILITIES"),
            ("dstv subscription", "UTILITIES"),
            ("gotv payment", "UTILITIES"),
            ("spectranet internet", "UTILITIES"),

            # Shopping
            ("j umia order", "SHOPPING"),
            ("konga purchase", "SHOPPING"),
            ("amazon online", "SHOPPING"),
            ("jiji buy", "SHOPPING"),
            ("slot phone", "SHOPPING"),
            ("computer village", "SHOPPING"),

            # Entertainment
            ("netflix subscription", "ENTERTAINMENT"),
            ("cinema payment", "ENTERTAINMENT"),
            ("spotify premium", "ENTERTAINMENT"),
            ("gaming purchase", "ENTERTAINMENT"),
            ("club entry", "ENTERTAINMENT"),

            # Healthcare
            ("pharmacy purchase", "HEALTHCARE"),
            ("hospital bill", "HEALTHCARE"),
            ("doctor consultation", "HEALTHCARE"),
            ("lab test payment", "HEALTHCARE"),
            ("medplus pharmacy", "HEALTHCARE"),

            # Salary/Income
            ("salary credit", "SALARY"),
            ("salary payment", "SALARY"),
            ("monthly salary", "SALARY"),
            ("payroll deposit", "SALARY"),

            # Freelance
            ("freelance payment", "FREELANCE"),
            ("upwork transfer", "FREELANCE"),
            ("fiverr earnings", "FREELANCE"),
            ("consulting fee", "FREELANCE"),
        ]

        self.pipeline = Pipeline([
            ('vectorizer', TfidfVectorizer(
                analyzer='word',
                ngram_range=(1, 2),
                lowercase=True,
                max_features=500
            )),
            ('classifier', MultinomialNB(alpha=0.5))
        ])

        # Train on initialization
        X = [item[0] for item in self.training_data]
        y = [item[1] for item in self.training_data]
        self.pipeline.fit(X, y)
        logger.info(f"Categorizer trained on {len(self.training_data)} samples")

    def predict(self, description: str, amount: float = None):
        """Predict category for a transaction description"""
        try:
            predicted = self.pipeline.predict([description.lower()])[0]
            probabilities = self.pipeline.predict_proba([description.lower()])[0]
            confidence = max(probabilities)

            return predicted, confidence
        except Exception as e:
            logger.error(f"Prediction error: {e}")
            return "OTHER", 0.5