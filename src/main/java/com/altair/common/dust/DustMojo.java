package com.altair.common.dust;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.Scanner;
import org.sonatype.plexus.build.incremental.BuildContext;

import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

/**
 * Goal which uses Dust.js to compile html template files into Dust scripts.
 *
 *
 * @author Dan Monroe
 */
@Mojo( name = "compile", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class DustMojo extends AbstractMojo {

    /**
     * The source directory containing the HTML template sources.
     */
    @Parameter( defaultValue = "${dust.sourceDirectory}", property = "sourceDirectory", required = true, alias = "sourrcedir")
    private File sourceDirectory;

    /**
     * The directory for compiled Dust templates.
     */
    @Parameter( defaultValue = "${dust.outputDirectory}", property = "outputDirectory", required = true, alias = "outputdir")
    private File outputDirectory;

    /**
     * List of files to include. Specified as fileset patterns which are relative to the source directory. Default value is: { "**\/*.html" }
     */
    @Parameter( property = "includes", defaultValue = "**/*.html")
    private String[] includes = new String[] { "**/*.html" };

    /**
     * List of files to exclude. Specified as fileset patterns which are relative to the source directory.
     */
    @Parameter( property = "excludes", required = false )
    private String[] excludes = new String[] {};

    /**
     * When <code>true</code> forces the Dust compiler to always compile the Dust sources. By default Dust sources are only compiled when modified or the output file does not exist.
     */
    @Parameter( defaultValue = "false", property = "force", required = false )
    private boolean force;

    @Component
    protected BuildContext buildContext;

    private DustCompiler dustCompiler = null;

    /**
     * Scans for the html template sources that should be compiled.
     *
     * @return The list of template sources.
     */
    protected String[] getIncludedFiles() {
        Scanner scanner = buildContext.newScanner(sourceDirectory, true);
        scanner.setIncludes(includes);
        scanner.setExcludes(excludes);
        scanner.scan();
        return scanner.getIncludedFiles();
    }

    public void execute() throws MojoExecutionException {
        long start = System.currentTimeMillis();

        if (getLog().isDebugEnabled()) {
            getLog().debug("sourceDirectory = " + sourceDirectory);
            getLog().debug("outputDirectory = " + outputDirectory);
            getLog().debug("includes = " + Arrays.toString(includes));
            getLog().debug("excludes = " + Arrays.toString(excludes));
        }


        String[] files = getIncludedFiles();

        if (files == null || files.length < 1) {
            getLog().info("Nothing to compile - no Dust template sources found");
        } else {
            if (getLog().isDebugEnabled()) {
                getLog().debug("included files = " + Arrays.toString(files));
            }


            try {
                dustCompiler = new DustCompiler();
            } catch (FileNotFoundException e) {
                throw new MojoExecutionException("Dust Compiler could not find the dust javascript source file. " + e.getMessage());
            } catch (ScriptException e) {
                throw new MojoExecutionException("Dust Compiler threw ScriptException. " + e.getMessage());
            } catch (IOException e) {
                throw new MojoExecutionException("Dust Compiler threw IOException. " + e.getMessage());
            }

            for (String file : files) {

                File input = new File(sourceDirectory, file);

                buildContext.removeMessages(input);

                File output = new File(outputDirectory, file.replace(".html", ".js"));

                if (!output.getParentFile().exists() && !output.getParentFile().mkdirs()) {
                    throw new MojoExecutionException("Cannot create output directory " + output.getParentFile());
                }

                try {
                    DustSource dustSource = new DustSource(input);

                    if (output.lastModified() < dustSource.getLastModified()) {
                        getLog().info("Compiling Dust template  source: " + file );
                        dustCompiler.compileAndSave(dustSource, output, force);

                        buildContext.refresh(output);
                    } else {
                        getLog().info("Bypassing Dust template source: " + file + " (not modified)");
                    }
                } catch (FileNotFoundException e) {
                    buildContext.addMessage(input, 0, 0, "Error compiling Dust template source", BuildContext.SEVERITY_ERROR, e);
                    throw new MojoExecutionException("Error while compiling Dust source: " + file, e);
                } catch (ScriptException e) {
                    buildContext.addMessage(input, 0, 0, "Error compiling Dust template source", BuildContext.SEVERITY_ERROR, e);
                    throw new MojoExecutionException("Error while compiling Dust source: " + file, e);
                } catch (NoSuchMethodException e) {
                    buildContext.addMessage(input, 0, 0, "Error compiling Dust template source", BuildContext.SEVERITY_ERROR, e);
                    throw new MojoExecutionException("Error while compiling Dust source: " + file, e);
                } catch (IOException e) {
                    buildContext.addMessage(input, 0, 0, "Error compiling Dust template source", BuildContext.SEVERITY_ERROR, e);
                    throw new MojoExecutionException("Error while compiling Dust source: " + file, e);
                }


            }

        }

        getLog().info("\n\nCompilation finished in " + (System.currentTimeMillis() - start) + " ms\n\n");

    }

}
