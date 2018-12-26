package skj.pro2.utils;

public class Log {

    private static final int maxTabs = 3; //Make sure any Tag is not longer that 3 tabs (21chars)

    public static void d(String message)
    {
        System.out.println(String.format("DEBUG: %s", message));
    }

    public static void e(String message)
    {
        System.out.println(String.format("ERROR: %s", message));
    }

    public static void d(String tag, String msg) { System.out.println(String.format("DEBUG[%s]%s%s", tag, getTabs(tag), msg)); }

    public static void e(String tag, String msg) { System.out.println(String.format("ERROR[%s]%s%s", tag, getTabs(tag), msg)); }

    public static void i(String tag, String msg) { System.out.println(String.format("INFO[%s]%s%s", tag, getTabs(tag), msg)); }

    private static String getTabs(String tag)
    {
        StringBuilder tabs = new StringBuilder();
        int tabsLen = maxTabs-(tag.length()/6);
        for(int i = 0; i < tabsLen; i++)
            tabs.append('\t');

        return tabs.toString();
    }
}
