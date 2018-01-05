package ui;

import actcomponents.ActComponent;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CommandLineParser {
    private final static String filePathOption = "-f";
    private final static String modeOption = "-m";
    private final static String rangeOption = "-r";

    /*
     * Parses argument from @args, given the key symbol for the argument @argName.
     */
    private String parseArg(String[] args, String argName) {
        boolean returnNext = false;
        for (String arg : args) {
            if (returnNext && (arg.equals(filePathOption) || arg.equals(rangeOption) || arg.equals(modeOption)))
                break;
            if (returnNext) return arg;
            if(arg.equals(argName)) returnNext = true;
        }
        throw new IllegalArgumentException("No option " + argName + " was specified");
    }

    String parseFilePath(String[] args) {
        return parseArg(args, filePathOption);
    }

    String parseRange(String[] args) {
        boolean foundMarker = false;
        String range = "";
        for (String arg : args) {
            if(foundMarker) {
                if (arg.equals(filePathOption) || arg.equals(modeOption)) break;
                range += range.isEmpty()? arg:" "+arg;
            }
            if(arg.equals(rangeOption)) foundMarker= true;
        }
        if(range.isEmpty()){
            throw new IllegalArgumentException("Range is empty.");
        } else {
            return range;
        }
    }

    String parseMode(String[] args) {
        return parseArg(args, modeOption);
    }
}
