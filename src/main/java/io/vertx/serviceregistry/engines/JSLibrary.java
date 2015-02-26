package io.vertx.serviceregistry.engines;

public class JSLibrary {
    final private String pathOnClasspath;
    final private String httpPath;

    public JSLibrary(String pathOnClasspath, String httpPath) {
        this.pathOnClasspath = pathOnClasspath;
        this.httpPath = httpPath;
    }

    public String getPathOnClasspath() {
        return pathOnClasspath;
    }

    public String getHttpPath() {
        return httpPath;
    }

    public String asScriptTag(boolean async) {
        StringBuilder sb = new StringBuilder("<script type=\"text/javascript\" src=\"");
        sb.append(httpPath + "\"");
        if (async) {
            sb.append(" async=true");
        }
        sb.append(" ></script>");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "StaticResource, classpath:" + pathOnClasspath + " | http:" + httpPath;
    }
}
