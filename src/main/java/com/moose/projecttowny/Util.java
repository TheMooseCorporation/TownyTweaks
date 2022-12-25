package com.moose.projecttowny;

import java.util.ArrayList;
import java.util.List;

public class Util {
    public static List<String> autoComplete(List<String> cmds, String arg) {
        List<String> out = new ArrayList<>();
        for (String cmd : cmds) {
            if (arg.startsWith(cmd.substring(0, arg.length())))
                out.add(cmd);
        }
        return out;
    }
}
