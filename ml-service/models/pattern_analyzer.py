from sklearn.cluster import KMeans
from sklearn.preprocessing import StandardScaler
import numpy as np
from collections import defaultdict
import logging

logger = logging.getLogger(__name__)

class SpendingPatternAnalyzer:
    """K-Means clustering for spending pattern analysis"""

    def __init__(self):
        self.scaler = StandardScaler()
        self.n_clusters = 3  # Low, Medium, High spenders

    def analyze(self, transactions):
        """Analyze spending patterns from transaction data"""
        try:
            if len(transactions) < 10:
                return [], ["Need at least 10 transactions for pattern analysis"], []

            # Aggregate spending by day of week
            day_spending = defaultdict(float)
            day_counts = defaultdict(int)
            category_spending = defaultdict(float)

            for txn in transactions:
                day = txn.get('day_of_week', 'Unknown')
                amount = abs(float(txn.get('amount', 0)))
                category = txn.get('category', 'OTHER')

                day_spending[day] += amount
                day_counts[day] += 1
                category_spending[category] += amount

            # Analyze patterns
            patterns = []
            insights = []
            recommendations = []

            # Day of week patterns
            if day_spending:
                max_day = max(day_spending, key=day_spending.get)
                avg_per_day = sum(day_spending.values()) / len(day_spending)

                patterns.append({
                    "type": "day_of_week",
                    "highest_spending_day": max_day,
                    "amount": round(day_spending[max_day], 2),
                    "average_per_day": round(avg_per_day, 2)
                })

                insights.append(f"Highest spending occurs on {max_day}")

                if day_spending[max_day] > avg_per_day * 1.5:
                    recommendations.append(
                        f"Consider reducing {max_day} expenses"
                    )

            # Category patterns
            if category_spending:
                sorted_cats = sorted(
                    category_spending.items(),
                    key=lambda x: x[1],
                    reverse=True
                )

                patterns.append({
                    "type": "category_breakdown",
                    "top_categories": [
                        {"category": cat, "amount": round(amt, 2)}
                        for cat, amt in sorted_cats[:5]
                    ]
                })

                top_category = sorted_cats[0]
                insights.append(
                    f"Highest spending category is {top_category[0]} "
                    f"(₦{top_category[1]:,.2f})"
                )

                if len(sorted_cats) > 1:
                    top_ratio = sorted_cats[0][1] / sum(
                        amt for _, amt in sorted_cats
                    )
                    if top_ratio > 0.4:
                        recommendations.append(
                            f"Your {sorted_cats[0][0]} spending is over "
                            f"40% of total. Consider budgeting carefully."
                        )

            return patterns, insights, recommendations

        except Exception as e:
            logger.error(f"Pattern analysis error: {e}")
            return [], [f"Analysis error: {str(e)}"], []