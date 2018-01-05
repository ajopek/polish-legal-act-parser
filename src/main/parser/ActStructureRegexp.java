package parser;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
/*
 * Defines regular expressions useful in parsing of legal act.
 */
public class ActStructureRegexp {
    public static final String polishWord = "([A-zĘęÓóĄąŚśŁłŻżŹźĆćŃń]+)";
    public static final String polishWordLowerCase = "([a-zęóąśłżźćń]+)";
    public static final String polishWordUpperCase = "([A-ZĘÓĄŚŁŻŹĆŃ]+)";
    public static final String polishUppercaseWords = "[A-ZĘÓĄŚŁŻŹĆŃ]+[A-ZĘÓĄŚŁŻŹĆŃ, ]+";
    public static final String polishWords = "[A-zĘęÓóĄąŚśŁłŻżŹźĆćŃń]+[A-zĘęÓóĄąŚśŁłŻżŹźĆćŃń, ]+";
    public static final String number = "([0-9]+)";
    public static final String componentId = "\\. ([0-9]+)([a-z]|)\\.(\\s|$)";
    public static final String constitutionDate = "^z dnia " + number + " "
                                                  + polishWordLowerCase + " " + number + " r.$";
}
