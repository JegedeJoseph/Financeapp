from sklearn.linear_model import LinearRegression
import numpy as np
from collections import defaultdict
import logging

logger = logging.getLogger(__name__)

class ExpenseForecaster:
    """Linear Regression for expense forecasting"""

    def __init__(self):
        self.model = LinearRegression()

    def predict(self, historical_spending):
        """Predict next month's expenses based on historical data"""
        try:
            if len(historical_spending) < 3:
                return {}, 0.0, 0.0

            # Aggregate total spending by month
            monthly_totals = defaultdict(float)
            monthly_category = defaultdict(lambda: defaultdict(float))

            for record in historical_spending:
                month = record.get('month', '')
                total = float(record.get('total', 0))
                monthly_totals[month] += total

                categories = record.get('categories', {})
                for cat, amt in categories.items():
                    monthly_category[month][cat] += float(amt)

            # Convert to ordered lists
            sorted_months = sorted(monthly_totals.keys())

            if len(sorted_months) < 3:
                # Not enough data - use simple average
                avg_total = np.mean(list(monthly_totals.values()))
                avg_by_category = {}
                for cat in set().union(*[d.keys() for d in monthly_category.values()]):
                    values = [monthly_category[m].get(cat, 0) for m in sorted_months]
                    avg_by_category[cat] = round(np.mean(values) if values else 0, 2)

                return avg_by_category, round(avg_total, 2), 0.3

            # Prepare features for linear regression
            X = np.arange(len(sorted_months)).reshape(-1, 1)
            y = np.array([monthly_totals[m] for m in sorted_months])

            # Fit model
            self.model.fit(X, y)

            # Predict next month
            next_month_idx = np.array([[len(sorted_months)]])
            predicted_total = float(self.model.predict(next_month_idx)[0])

            # Predict by category (simple trend continuation)
            predicted_categories = {}
            for cat in set().union(*[d.keys() for d in monthly_category.values()]):
                cat_values = [monthly_category[m].get(cat, 0) for m in sorted_months]
                if len(cat_values) >= 2:
                    # Simple moving average
                    weights = [0.3, 0.3, 0.4] if len(cat_values) >= 3 else [0.4, 0.6]
                    weighted_avg = sum(
                        v * w for v, w in zip(cat_values[-len(weights):], weights[-len(cat_values):])
                    )
                else:
                    weighted_avg = cat_values[-1] if cat_values else 0

                predicted_categories[cat] = round(weighted_avg, 2)

            # Calculate confidence based on R² score
            r2_score = self.model.score(X, y)
            confidence = min(max(r2_score, 0.2), 0.95)

            return predicted_categories, predicted_total, confidence

        except Exception as e:
            logger.error(f"Forecast error: {e}")
            return {}, 0.0, 0.0