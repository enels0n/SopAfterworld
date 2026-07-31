package net.enelson.sopafterworld.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Small expression parser for configurable math formulas.
 * Supports numbers, parentheses, + - * / ^, variable names and simple functions.
 */
public final class MathExpression {

	private final String expression;
	private final String source;
	private int index;

	private MathExpression(String expression) {
		this.expression = expression;
		this.source = expression;
	}

	public static double evaluate(String expression, double deaths) {
		MathExpression parser = new MathExpression(expression);
		double result = parser.parseExpression(deaths);
		parser.skipWhitespace();
		if (!parser.isEnd()) {
			throw new IllegalArgumentException("Unexpected token at position " + parser.index);
		}
		return result;
	}

	private double parseExpression(double deaths) {
		double value = parseTerm(deaths);
		while (true) {
			skipWhitespace();
			if (match('+')) {
				value += parseTerm(deaths);
			} else if (match('-')) {
				value -= parseTerm(deaths);
			} else {
				return value;
			}
		}
	}

	private double parseTerm(double deaths) {
		double value = parsePower(deaths);
		while (true) {
			skipWhitespace();
			if (match('*')) {
				value *= parsePower(deaths);
			} else if (match('/')) {
				value /= parsePower(deaths);
			} else {
				return value;
			}
		}
	}

	private double parsePower(double deaths) {
		double base = parseUnary(deaths);
		skipWhitespace();
		if (match('^')) {
			double exponent = parsePower(deaths);
			return Math.pow(base, exponent);
		}
		return base;
	}

	private double parseUnary(double deaths) {
		skipWhitespace();
		if (match('+')) {
			return parseUnary(deaths);
		}
		if (match('-')) {
			return -parseUnary(deaths);
		}
		return parsePrimary(deaths);
	}

	private double parsePrimary(double deaths) {
		skipWhitespace();
		if (match('(')) {
			double value = parseExpression(deaths);
			expect(')');
			return value;
		}

		if (isDigit(peek()) || peek() == '.') {
			return parseNumber();
		}

		if (isIdentifierStart(peek())) {
			String identifier = parseIdentifier();
			skipWhitespace();
			if (match('(')) {
				List<Double> arguments = new ArrayList<Double>();
				skipWhitespace();
				if (!match(')')) {
					do {
						arguments.add(parseExpression(deaths));
						skipWhitespace();
					} while (match(','));
					expect(')');
				}
				return applyFunction(identifier, arguments);
			}
			return resolveVariable(identifier, deaths);
		}

		throw new IllegalArgumentException("Unexpected token at position " + index + " in '" + source + "'");
	}

	private double parseNumber() {
		int start = index;
		while (!isEnd() && (isDigit(peek()) || peek() == '.')) {
			index++;
		}
		return Double.parseDouble(expression.substring(start, index));
	}

	private String parseIdentifier() {
		int start = index;
		while (!isEnd() && isIdentifierPart(peek())) {
			index++;
		}
		return expression.substring(start, index).toLowerCase(Locale.ROOT);
	}

	private double resolveVariable(String identifier, double deaths) {
		if ("deaths".equals(identifier)) {
			return deaths;
		}
		if ("pi".equals(identifier)) {
			return Math.PI;
		}
		if ("e".equals(identifier)) {
			return Math.E;
		}
		throw new IllegalArgumentException("Unknown variable: " + identifier);
	}

	private double applyFunction(String identifier, List<Double> arguments) {
		if ("sin".equals(identifier)) {
			return singleArg(identifier, arguments, Math::sin);
		}
		if ("cos".equals(identifier)) {
			return singleArg(identifier, arguments, Math::cos);
		}
		if ("tan".equals(identifier)) {
			return singleArg(identifier, arguments, Math::tan);
		}
		if ("asin".equals(identifier)) {
			return singleArg(identifier, arguments, Math::asin);
		}
		if ("acos".equals(identifier)) {
			return singleArg(identifier, arguments, Math::acos);
		}
		if ("atan".equals(identifier)) {
			return singleArg(identifier, arguments, Math::atan);
		}
		if ("sqrt".equals(identifier)) {
			return singleArg(identifier, arguments, Math::sqrt);
		}
		if ("abs".equals(identifier)) {
			return singleArg(identifier, arguments, Math::abs);
		}
		if ("floor".equals(identifier)) {
			return singleArg(identifier, arguments, Math::floor);
		}
		if ("ceil".equals(identifier)) {
			return singleArg(identifier, arguments, Math::ceil);
		}
		if ("round".equals(identifier)) {
			return singleArg(identifier, arguments, value -> (double) Math.round(value));
		}
		if ("log".equals(identifier)) {
			return singleArg(identifier, arguments, Math::log);
		}
		if ("log10".equals(identifier)) {
			return singleArg(identifier, arguments, Math::log10);
		}
		if ("exp".equals(identifier)) {
			return singleArg(identifier, arguments, Math::exp);
		}
		if ("min".equals(identifier)) {
			return multiArgMinMax(identifier, arguments, true);
		}
		if ("max".equals(identifier)) {
			return multiArgMinMax(identifier, arguments, false);
		}
		if ("pow".equals(identifier)) {
			requireArgs(identifier, arguments, 2);
			return Math.pow(arguments.get(0), arguments.get(1));
		}
		if ("clamp".equals(identifier)) {
			requireArgs(identifier, arguments, 3);
			return Math.max(arguments.get(1), Math.min(arguments.get(2), arguments.get(0)));
		}
		throw new IllegalArgumentException("Unknown function: " + identifier);
	}

	private double singleArg(String identifier, List<Double> arguments, DoubleUnaryOperator operator) {
		requireArgs(identifier, arguments, 1);
		return operator.apply(arguments.get(0));
	}

	private double multiArgMinMax(String identifier, List<Double> arguments, boolean min) {
		if (arguments.isEmpty()) {
			throw new IllegalArgumentException("Function " + identifier + " requires at least 1 argument");
		}
		double result = arguments.get(0);
		for (int i = 1; i < arguments.size(); i++) {
			result = min ? Math.min(result, arguments.get(i)) : Math.max(result, arguments.get(i));
		}
		return result;
	}

	private void requireArgs(String identifier, List<Double> arguments, int expected) {
		if (arguments.size() != expected) {
			throw new IllegalArgumentException("Function " + identifier + " requires " + expected + " arguments");
		}
	}

	private void expect(char expected) {
		skipWhitespace();
		if (!match(expected)) {
			throw new IllegalArgumentException("Expected '" + expected + "' at position " + index);
		}
	}

	private boolean match(char expected) {
		if (peek() == expected) {
			index++;
			return true;
		}
		return false;
	}

	private void skipWhitespace() {
		while (!isEnd() && Character.isWhitespace(expression.charAt(index))) {
			index++;
		}
	}

	private char peek() {
		return isEnd() ? '\0' : expression.charAt(index);
	}

	private boolean isEnd() {
		return index >= expression.length();
	}

	private boolean isDigit(char value) {
		return value >= '0' && value <= '9';
	}

	private boolean isIdentifierStart(char value) {
		return Character.isLetter(value) || value == '_';
	}

	private boolean isIdentifierPart(char value) {
		return Character.isLetterOrDigit(value) || value == '_';
	}

	private interface DoubleUnaryOperator {
		double apply(double value);
	}
}
