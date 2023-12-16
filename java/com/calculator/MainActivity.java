package com.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView calculationTV, resultTV;
    MaterialButton buttonCurrencyConverter, buttonD, buttonC, buttonOpenParenthesis, buttonCloseParenthesis, buttonDecimalPoint, buttonEquals;
    MaterialButton buttonDivision, buttonMultiplication, buttonSubtraction, buttonAddition;
    MaterialButton button0, button1, button2, button3, button4, button5, button6, button7, button8, button9;
    Boolean decimalPointAllowed = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get TextViews from xml
        calculationTV = findViewById(R.id.calculationTV);
        resultTV = findViewById(R.id.resultTV);

        // get the MaterialButtons from xml
        assignAllMaterialButtons();

        // switch to converter activity
        buttonCurrencyConverter = findViewById(R.id.button_currency_converter);
        buttonCurrencyConverter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchActivities();
            }
        });
    }

    private void switchActivities() {
        Intent switchActivityIntent = new Intent(this, ConverterActivity.class);
        startActivity(switchActivityIntent);
    }

    void assignID(MaterialButton btn, int id) {
        btn = findViewById(id);
        btn.setOnClickListener(this);
    }

    void assignAllMaterialButtons() {
        assignID(buttonCurrencyConverter, R.id.button_currency_converter);
        assignID(buttonD, R.id.button_delete);
        assignID(buttonC, R.id.button_c);
        assignID(buttonOpenParenthesis, R.id.button_open_parenthesis);
        assignID(buttonCloseParenthesis, R.id.button_close_parenthesis);
        assignID(buttonDecimalPoint, R.id.button_decimal_point);
        assignID(buttonEquals, R.id.button_equals);

        assignID(buttonDivision, R.id.button_division);
        assignID(buttonMultiplication, R.id.button_multiplication);
        assignID(buttonSubtraction, R.id.button_subtraction);
        assignID(buttonAddition, R.id.button_addition);

        assignID(button0, R.id.button_0);
        assignID(button1, R.id.button_1);
        assignID(button2, R.id.button_2);
        assignID(button3, R.id.button_3);
        assignID(button4, R.id.button_4);
        assignID(button5, R.id.button_5);
        assignID(button6, R.id.button_6);
        assignID(button7, R.id.button_7);
        assignID(button8, R.id.button_8);
        assignID(button9, R.id.button_9);
    }

    @Override
    public void onClick(View view) {
        // get clicked button's content
        MaterialButton btn = (MaterialButton) view;
        String btnText = btn.getText().toString();

        // if the currency conversion does not take place
        if (!btnText.equals("CurrencyConverter")) {
            // get all previous content
            String prevData = calculationTV.getText().toString();
            // send all collected data to calculate
            calculate(prevData, btnText);
        }
    }

    public void calculate(String prevData, String newData) {
        // check if the newly pressed button was C (delete all characters)
        if (newData.equals("C")) {
            decimalPointAllowed = true;
            calculationTV.setText("");
            resultTV.setText("0");
            return;
        }

        // check if the newly pressed button was = (show result)
        if (newData.equals("=")) {
            String res = resultTV.getText().toString();
            res = res.replace(",",".");
            calculationTV.setText(res);
            return;
        }

        // add a zero in front of the decimal when there isn't any and prevData is empty
        if (newData.endsWith(".") && prevData.length() == 0) {
            decimalPointAllowed = false;
            calculationTV.setText("0.");
            resultTV.setText("");
            return;
        }

        // don't allow second decimal point
        if (newData.endsWith(".")) {
            if (prevData.endsWith(".")) {
                decimalPointAllowed = false;
                calculationTV.setText(prevData);
                resultTV.setText("");
            } else if (decimalPointAllowed) {
                System.out.println("decimalPointAllowed1:"+decimalPointAllowed);
                newData = prevData + newData;
                calculationTV.setText(newData);
                setResult(newData);
                decimalPointAllowed = false;
            } else if (!decimalPointAllowed) {
                System.out.println("decimalPointAllowed2:"+decimalPointAllowed);
                newData = prevData;
                calculationTV.setText(newData);
                setResult(newData);
            }
            return;
        }

        /*for (int i=0; i<prevData.length(); i++) {
            if (prevData.charAt(i) == '+' || prevData.charAt(i) == '-' || prevData.charAt(i) == '*'
                    || prevData.charAt(i) == '/' || prevData.charAt(i) == '(' || prevData.charAt(i) == ')'
                    || prevData.charAt(i) == '.') {

                if (prevData.charAt(i) == '(') {

                } else if (prevData.charAt(i) == ')') {

                }
            } else {

            }
            calculationTV.setText(prevData);
            String res = resultTV.getText().toString();
            resultTV.setText(res);
            return;
        }*/

        // add a zero in front of the decimal when there isn't any
        if (!prevData.endsWith("0") && !prevData.endsWith("1") && !prevData.endsWith("2") && !prevData.endsWith("3")
                && !prevData.endsWith("4") && !prevData.endsWith("5") && !prevData.endsWith("6") && !prevData.endsWith("7")
                && !prevData.endsWith("8") && !prevData.endsWith("9")) {
            if (newData.equals(".")) {
                if (prevData.endsWith(")")) {
                    decimalPointAllowed = true;
                    calculationTV.setText(prevData + "*0.");
                } else {
                    decimalPointAllowed = true;
                    calculationTV.setText(prevData + "0.");
                }
                resultTV.setText("");
                return;
            }
        }

        // check if the newly pressed button was D (delete one character)
        if (newData.equals("D")) {
            if (prevData.length() == 0 || prevData.length() == 1) {
                decimalPointAllowed = true;
                calculationTV.setText("");
                resultTV.setText("0");
            } else {
                String lastPrevData = prevData.substring(prevData.length() - 1, prevData.length());
                if (lastPrevData.equals(".")) {
                    decimalPointAllowed = true;
                }
                prevData = prevData.substring(0, prevData.length() - 1);
                newData = prevData;
                calculationTV.setText(newData);
                // set the result
                setResult(newData);
            }
            return;
        }

        // if zero is the first digit, don't allow second zero to be added
        if (prevData.startsWith("0") && prevData.length() == 1) {
            if (newData.equals(".") || newData.equals("+") || newData.equals("-")
                    || newData.equals("*") || newData.equals("/")) {
                if (newData.equals(".")) {
                    decimalPointAllowed = false;
                }
                newData = prevData + newData;
            } else if (newData.equals(")")) {
                decimalPointAllowed = true;
                newData = prevData;
            } else if (newData.equals("(")) {
                decimalPointAllowed = true;
                newData = prevData + "*" + newData;
                if (newData.equals(".")) {
                    decimalPointAllowed = true;
                }
            }

            calculationTV.setText(newData);

            // set the result
            setResult(newData);
            return;
        }

        /* if there is a decimal point followed by a symbol,
         * keep the latest */
        if (prevData.endsWith(".")) {
            if (newData.endsWith("(")) {
                decimalPointAllowed = true;

                // remove the decimal point
                prevData = prevData.substring(0, prevData.length() - 1);

                // add multiplication with the parenthesis
                newData = prevData + "*(";
                calculationTV.setText(newData);

                // set the result
                setResult(newData);
                return;
            } else if (newData.endsWith("+") || newData.endsWith("-") || newData.endsWith("*") || newData.endsWith("/")) {
                decimalPointAllowed = true;

                // remove the decimal point and replace it with the symbol
                prevData = prevData.substring(0, prevData.length() - 1);

                newData = prevData + newData;
                calculationTV.setText(newData);

                // set the result
                setResult(newData);
                return;
            }
        }

        // if max length reached, don't allow more digits
        int numOfNumbers = 0, numOfSigns = 0, numOfOpenParenthesis = 0, numOfClosedParenthesis = 0;
        for (int i=0; i<prevData.length(); i++) {
            if (prevData.charAt(i) == '+' || prevData.charAt(i) == '-' || prevData.charAt(i) == '*'
                    || prevData.charAt(i) == '/' || prevData.charAt(i) == '(' || prevData.charAt(i) == ')'
                    || prevData.charAt(i) == '.') {
                numOfSigns++;
                if (prevData.charAt(i) == '(') {
                    numOfOpenParenthesis++;
                } else if (prevData.charAt(i) == ')') {
                    numOfClosedParenthesis++;
                }
            } else {
                numOfNumbers++;
            }
        }
        if (numOfNumbers >= 20 || numOfSigns >= 20) {
            newData = prevData;

            // let the user know they exceeded the limit
            Snackbar.make(findViewById(R.id.relative_layout_calculator), R.string.maximumLengthMessage, Snackbar.LENGTH_SHORT).show();

            calculationTV.setText(newData);

            // set the result
            setResult(newData);
            return;
        }

        // add "*" automatically before or after a parenthesis, if only there isn't a sign already there
        if ( (prevData.endsWith("0") || prevData.endsWith("1") || prevData.endsWith("2")
                || prevData.endsWith("3") || prevData.endsWith("4") || prevData.endsWith("5")
                || prevData.endsWith("6") || prevData.endsWith("7") || prevData.endsWith("8")
                || prevData.endsWith("9") || prevData.endsWith(")"))
                && newData.endsWith("(") ) {
            decimalPointAllowed = true;
            newData = prevData + "*(";
        } else if ( prevData.endsWith(")") && ( newData.endsWith("0") || newData.endsWith("1") || newData.endsWith("2")
                || newData.endsWith("3") || newData.endsWith("4") || newData.endsWith("5")
                || newData.endsWith("6") || newData.endsWith("7") || newData.endsWith("8")
                || newData.endsWith("9") || newData.endsWith(".") ) ) {
            decimalPointAllowed = true;
            newData = prevData + "*" + newData;

        // check the parenthesis's right closure
        } else if (newData.endsWith(")")) {
            if (numOfClosedParenthesis < numOfOpenParenthesis) {
                if (prevData.endsWith(".") || prevData.endsWith("+") || prevData.endsWith("-") || prevData.endsWith("*") || prevData.endsWith("/")) {
                    decimalPointAllowed = true;
                    prevData = prevData.substring(0, prevData.length() - 1);
                }
                newData = prevData + newData;
            } else {
                newData = prevData;
            }

        // check if there are multiple signs altogether, keep the latest
        } else if (newData.endsWith("+") || newData.endsWith("-") || newData.endsWith("*") || newData.endsWith("/")) {
            decimalPointAllowed = true;
            if (prevData.endsWith("+") || prevData.endsWith("-") || prevData.endsWith("*") || prevData.endsWith("/")) {
                // save previous sign
                String lastPrevData = prevData.substring(prevData.length() - 1, prevData.length());
                // remove previous sign
                prevData = prevData.substring(0, prevData.length() - 1);

                if (prevData.endsWith("(")) {
                    newData = prevData + lastPrevData;

                    // let the user know this is an invalid format
                    Snackbar.make(findViewById(R.id.relative_layout_calculator), R.string.invalidFormatMessage, Snackbar.LENGTH_SHORT).show();
                } else {
                    // find latest sign
                    newData = newData.substring(newData.length() - 1, newData.length());

                    // merge the previous data with the new sign
                    newData = prevData + newData;
                }
            } else if (prevData.endsWith("(")) {
                if (newData.endsWith("*") || newData.endsWith("/")) {
                    newData = prevData;

                    // let the user know this is an invalid format
                    Snackbar.make(findViewById(R.id.relative_layout_calculator), R.string.invalidFormatMessage, Snackbar.LENGTH_SHORT).show();
                } else {
                    newData = prevData + newData;
                }
            } else {
                newData = prevData + newData;
            }

        } else {
            newData = prevData + newData;
        }

        // insert new data to the calculation
        calculationTV.setText(newData);

        // set the result
        setResult(newData);
    }

    void setResult(String data) {
        String finalResult = null;

        try {
            Context context = Context.enter();
            context.setOptimizationLevel(-1);
            Scriptable scriptable = context.initSafeStandardObjects();
            finalResult = context.evaluateString(scriptable, data, "Javascript", 1, null).toString();
            if (finalResult.endsWith(".0")) {
                // don't show the decimal part if it's zero
                finalResult = finalResult.replace(".0", "");
            } else {
                // show max 5 decimal digits
                DecimalFormat df = new DecimalFormat("#.#####");
                Double finalResultDouble = Double.parseDouble(finalResult);

                finalResult = df.format(finalResultDouble);
            }
        } catch (Exception e) {
            // nan: not a number
            finalResult = "nan";
        }

        // check if the result is a number or not, and show the corresponding result
        if (finalResult.equals("nan") || data.endsWith("+") || data.endsWith("-") || data.endsWith("*")
                || data.endsWith("/") || data.endsWith("(") || data.endsWith(".")) {
            // if the last digit is either nan or a sign, don't show anything
            resultTV.setText("");
        } else if (finalResult.equals("Infinity") || data.contains("/0")) {
            // division by zero, undefined
            resultTV.setText("undefined");
        } else {
            // show numerical result
            resultTV.setText(finalResult);
        }
    }
}