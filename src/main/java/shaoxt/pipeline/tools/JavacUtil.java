/**
 * Copyright (c) 2016 eBay Software Foundation. All rights reserved.
 * <p>
 * Licensed under the MIT license.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * <p>
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package shaoxt.pipeline.tools;

import javax.tools.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Simple Javac implementation
 *
 * @author Sheldon Shao xshao@ebay.com on 3/5/18.
 * @version 1.0
 */
public class JavacUtil {

    //TODO simple implementation
    public static boolean javac(File dir) throws Exception {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(dir, "_javac_output.log"));
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(
                            new BufferedOutputStream(fos), "utf8"));
            PrintWriter out = new PrintWriter(writer);

            List<String> options = new ArrayList<>();
            options.add("-classpath");
            options.add(dir.getAbsolutePath());

            JavaCompiler javac = ToolProvider.getSystemJavaCompiler();

            //TODO fixed charset
            Charset utf8 = Charset.forName("utf8");
            DiagnosticCollector diagnosticCollector = new DiagnosticCollector();

            try (StandardJavaFileManager fm = javac.getStandardFileManager(diagnosticCollector,
                    Locale.getDefault(), utf8)) {

                File[] javaFiles = dir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".java");
                    }
                });

                Iterable<? extends JavaFileObject> files = fm.getJavaFileObjects(javaFiles);

                //TODO no library for demo
                JavaCompiler.CompilationTask compilationTask
                        = javac.getTask(out, fm, diagnosticCollector, options,
                        Collections.emptyList(), files);

                //TODO sync way
                Boolean res = compilationTask.call();
                return (res != null && res);
            }
        }
        finally {
            if (fos != null) {
                fos.close();
            }
        }
    }
}
