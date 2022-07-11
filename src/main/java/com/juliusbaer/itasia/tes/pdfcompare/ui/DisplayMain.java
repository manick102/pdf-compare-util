package com.juliusbaer.itasia.tes.pdfcompare.ui;

import com.juliusbaer.itasia.tes.pdfcompare.cli.CliArguments;
import com.juliusbaer.itasia.tes.pdfcompare.cli.CliArgumentsParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisplayMain {

    private static final Logger LOG = LoggerFactory.getLogger(DisplayMain.class);

    public static void main(String[] args) {
        try {
            final CliArguments cliArguments = new CliArguments(args);

            final Display display = new Display();
            if (cliArguments.hasFileArguments()) {
                display.init(cliArguments);
            } else {
                display.init();
            }
        } catch (CliArgumentsParseException exception) {
            LOG.error(exception.getMessage());
        }
    }
}
