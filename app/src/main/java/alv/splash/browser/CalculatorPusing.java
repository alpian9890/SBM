package alv.splash.browser;

import android.content.Context;
import android.widget.Toast;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class CalculatorPusing {
    private final Context context;

    public CalculatorPusing(Context context) {
        this.context = context;
    }

    public String calculate(String expressionStr) {
        // Validasi input kosong
        if (expressionStr == null || expressionStr.trim().isEmpty()) {
            showError("Ekspresi tidak boleh kosong");
            return null;
        }

        // Validasi karakter yang diizinkan (angka, operator, dan titik desimal)
        if (!expressionStr.matches("[0-9+\\-*/.()]+")) {
            showError("Karakter tidak valid terdeteksi");
            return null;
        }

        try {
            // Membuat ekspresi dan menghitung
            Expression expression = new ExpressionBuilder(expressionStr).build();
            double result = expression.evaluate();

            // Handle angka desimal
            if (result == (int) result) {
                return String.valueOf((int) result);
            } else {
                return String.valueOf(result);
            }
        } catch (ArithmeticException | IllegalArgumentException e) {
            showError("Ekspresi matematika tidak valid");
            return null;
        }
    }

    public void showError(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
