package com.cts.mfrp.petz.tests.smoke;

import com.cts.mfrp.petz.base.BaseTest;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.cts.mfrp.petz.constants.AppConstants.HOME_URL;
import static com.cts.mfrp.petz.constants.AppConstants.LOGIN_URL;
import static com.cts.mfrp.petz.constants.AppConstants.REGISTER_URL;

/**
 * Quick diagnostic — opens each public page, waits for the SPA to render, then dumps
 * the rendered DOM to test-output/dom/ so we can see the actual selectors.
 */
public class DomDump extends BaseTest {

    @Test(priority = 1)
    public void dumpLanding()  throws Exception { dump("landing",  HOME_URL); }

    @Test(priority = 2)
    public void dumpLogin()    throws Exception { dump("login",    LOGIN_URL); }

    @Test(priority = 3)
    public void dumpRegister() throws Exception { dump("register", REGISTER_URL); }

    private void dump(String name, String url) throws Exception {
        driver.get(url);
        Thread.sleep(3000); // give Angular time to render
        String html = driver.getPageSource();
        String title = driver.getTitle();

        Path outDir = Path.of("test-output", "dom");
        Files.createDirectories(outDir);
        Path out = outDir.resolve(name + ".html");
        Files.writeString(out, html);
        System.out.println("[dom] " + name + " title=" + title + " bytes=" + html.length() + " -> " + out.toAbsolutePath());
    }
}
