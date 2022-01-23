package com.cleanroommc.standalone.utils;

import org.apache.logging.log4j.Logger;

public class StandaloneLog {

    public static Logger logger;

    public StandaloneLog() {
        // empty constructor
    }

    public static void init(Logger modLogger) {
        logger = modLogger;
    }
}
