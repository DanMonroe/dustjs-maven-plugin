dustjs-maven-plugin
===========

Maven plugin for the Linkedin version of Dust.js

This plugin will precompile dust templates into their javascript format for use in your projects.

When you need to use a dust template, create the template and place it into the sourceDirectory configured in your projects pom file. 
The name of the template file will be the registered dust template name used in the dust.render call.  For example, calendar_event_template.tl will register as calendar_event_template

You may run the maven dust compile target with the command: mvn dust:compile

This will place a corresponding .js file into the outputDirectory.

You can now just include this script onto your page and render it where needed.

    <script type="text/javascript" src="/scripts/dust-core-1.2.2.js"></script>
    <script type="text/javascript" src="/scripts/templates/dust/js/calendar_event_template.js"></script>
    
in javascript, after you have retrieved some sort of json object, objMyEvents in this case, call dust.render with the data you want to display:

       dust.render("calendar_event_template", objMyEvents, function(err, out) {
            $('#myEventsWrapper').html(out);
        });

All configuration options
-------------------------

+ outputDirectory (File) - The directory for compiled javascript templates. 
+ sourceDirectory (File) - The source directory containing the LESS sources.
+ includes (String[]) - List of files to include. Specified as fileset patterns which are relative to the source directory. Default value is: { "**\/*.html" }
+ excludes (String[]) - List of files to exclude. Specified as fileset patterns which are relative to the source directory.
+ force (boolean) - When true forces the Dust.js compiler to always compile the HTML templates. By default templates are only compiled when modified or the compliled javascript template does not exists. Default value is: false.

Example pom.xml configuration:

    <plugin>
	    <groupId>com.altair.common.dust</groupId>
	    <artifactId>dust-maven-plugin</artifactId>
	    <version>1.0.0</version>
	    <configuration>
	        <sourceDirectory>src/main/webapp/scripts/templates/dust/source</sourceDirectory>
	        <outputDirectory>src/main/webapp/scripts/templates/dust/js</outputDirectory>
          <includes>
            <include>**/*.tl</include>
          </includes>
	    </configuration>
	    <executions>
	        <execution>
	            <goals>
	                <goal>compile</goal>
	            </goals>
	        </execution>
	    </executions>
	  </plugin>
