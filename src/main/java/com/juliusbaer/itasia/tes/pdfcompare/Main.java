package com.juliusbaer.itasia.tes.pdfcompare;

import com.juliusbaer.itasia.tes.pdfcompare.cli.CliArguments;
import com.juliusbaer.itasia.tes.pdfcompare.cli.CliArgumentsParseException;
import com.juliusbaer.itasia.tes.pdfcompare.ui.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            final CliArguments cliArguments = new CliArguments(args);

            if (args.length > 0) {
                System.exit(cliArguments.execute());
            } else {
                startUI();
            }
        } catch (CliArgumentsParseException exception) {
            LOG.error(exception.getMessage());
        }
    }

    private static void startUI() {
        new Display().init();
    }
}
