package com.altair.common.dust;


import org.apache.commons.io.FileUtils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: dmonroe
 * Date: 4/10/13
 * Time: 3:14 PM
 */
public class DustCompiler {
    private ScriptEngineManager mgr = new ScriptEngineManager();
    private ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
    Invocable invocable;

    String dustFilename = "META-INF/dust-full-1.2.2.js";  // LinkedIn version https://raw.github.com/linkedin/dustjs/master/dist/dust-full-1.2.2.js

    public DustCompiler() throws IOException, ScriptException {

        ClassLoader loader = getClass().getClassLoader();
        URL resource = loader.getResource(dustFilename);

        InputStreamReader inputStreamReader = new InputStreamReader(resource.openConnection().getInputStream());

        try {
            jsEngine.eval(inputStreamReader);
        } finally {
            inputStreamReader.close();
        }

        invocable = (Invocable) jsEngine;
    }

    public String compile(DustSource sourceFile) throws FileNotFoundException, ScriptException, NoSuchMethodException {
        return compile(sourceFile.getContents(), sourceFile.getBaseName());
    }

    public String compile(String templateToParse, String templateName) throws FileNotFoundException, ScriptException, NoSuchMethodException {

        Object dustObj = getDustObjectFromEngine();

        Object[] params = new Object[]{templateToParse, templateName};

        Object returnObj = invocable.invokeMethod(dustObj, "compile", params);

        String result = (String)returnObj;
        return result;
    }

    private Object getDustObjectFromEngine() {
        Object dustObj = jsEngine.get("dust");
        return  dustObj;
    }

    public void compileAndSave(DustSource input, File output, boolean force) throws IOException, ScriptException, NoSuchMethodException {
        if (force || !output.exists() || output.lastModified() < input.getLastModified()) {
            String compiledTemplate = compile(input);
            FileUtils.writeStringToFile(output, compiledTemplate, "UTF-8");
        }
    }
}
