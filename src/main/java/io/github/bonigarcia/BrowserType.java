/*
 * (C) Copyright 2017 Boni Garcia (http://bonigarcia.github.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.github.bonigarcia;

import static io.github.bonigarcia.SeleniumJupiter.getString;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static org.openqa.selenium.Platform.ANY;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import io.github.bonigarcia.DockerBrowserConfig.Browser;
import io.github.bonigarcia.DockerBrowserConfig.BrowserConfig;
import io.github.bonigarcia.handler.ChromeDriverHandler;
import io.github.bonigarcia.handler.DriverHandler;
import io.github.bonigarcia.handler.FirefoxDriverHandler;
import io.github.bonigarcia.handler.OperaDriverHandler;

/**
 * Enumeration for Selenoid browsers.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.2.0
 */
public enum BrowserType {

    CHROME, FIREFOX, OPERA;

    String dockerImage;
    Class<? extends RemoteWebDriver> driverClass;
    String latestVersion;
    String firstVersion;
    DriverHandler driverHandler;
    String optionsKey;

    public BrowserConfig getBrowserConfigFromProperties() {
        switch (this) {
        case FIREFOX:
            firstVersion = getString("sel.jup.firefox.first.version");
            latestVersion = getString("sel.jup.firefox.latest.version");
            dockerImage = getString("sel.jup.firefox.image.format");
            driverClass = DockerFirefoxDriver.class;
            driverHandler = FirefoxDriverHandler.getInstance();
            optionsKey = FirefoxOptions.FIREFOX_OPTIONS;
            break;
        case OPERA:
            firstVersion = getString("sel.jup.opera.first.version");
            latestVersion = getString("sel.jup.opera.latest.version");
            dockerImage = getString("sel.jup.opera.image.format");
            driverClass = DockerOperaDriver.class;
            driverHandler = OperaDriverHandler.getInstance();
            optionsKey = OperaOptions.CAPABILITY;
            break;
        case CHROME:
        default:
            firstVersion = getString("sel.jup.chrome.first.version");
            latestVersion = getString("sel.jup.chrome.latest.version");
            dockerImage = getString("sel.jup.chrome.image.format");
            driverClass = DockerChromeDriver.class;
            driverHandler = ChromeDriverHandler.getInstance();
            optionsKey = ChromeOptions.CAPABILITY;
            break;
        }

        BrowserConfig browserConfig = new BrowserConfig(latestVersion);
        String version = firstVersion;
        do {
            browserConfig.addBrowser(version,
                    new Browser(format(dockerImage, version)));
            if (version.equals(latestVersion)) {
                break;
            }
            version = getNextVersion(version, latestVersion);
        } while (version != null);

        return browserConfig;
    }

    public String getDockerImage(String version) {
        return String.format(getDockerImage(), version);
    }

    public String getDockerImage() {
        return dockerImage;
    }

    public Class<? extends RemoteWebDriver> getDriverClass() {
        return driverClass;
    }

    public DesiredCapabilities getCapabilities() {
        switch (this) {
        case FIREFOX:
            return new DesiredCapabilities("firefox", "", ANY);
        case OPERA:
            return new DesiredCapabilities("operablink", "", ANY);
        case CHROME:
        default:
            return new DesiredCapabilities("chrome", "", ANY);
        }
    }

    public DriverHandler getDriverHandler() {
        return driverHandler;
    }

    public String getOptionsKey() {
        return optionsKey;
    }

    public static String getNextVersion(String version, String latestVersion) {
        int iVersion = version.indexOf('.');
        iVersion = iVersion != -1 ? iVersion : version.length();
        int nextVersionInt = parseInt(version.substring(0, iVersion)) + 1;

        int iLatestVersion = latestVersion.indexOf('.');
        iLatestVersion = iLatestVersion != -1 ? iLatestVersion
                : latestVersion.length();
        int latestVersionInt = parseInt(
                latestVersion.substring(0, iLatestVersion)) + 1;

        if (nextVersionInt > latestVersionInt) {
            return null;
        }
        return String.valueOf(nextVersionInt) + ".0";
    }

}