package com.wikia.webdriver.testcases.adstests;

import com.wikia.webdriver.common.dataprovider.ads.AdsDataProvider;
import com.wikia.webdriver.common.dataprovider.mobile.MobileAdsDataProvider;
import com.wikia.webdriver.common.logging.PageObjectLogging;
import com.wikia.webdriver.common.templates.TemplateDontLogout;
import com.wikia.webdriver.pageobjectsfactory.pageobject.adsbase.AdsBaseObject;
import com.wikia.webdriver.pageobjectsfactory.pageobject.adsbase.mobile.MobileAdsBaseObject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

public class TestFloorAdhesion extends TemplateDontLogout {
    private final String FLOOR_ADHESION_SELECTOR = "#ext-wikia-adEngine-template-footer";
    private final String FLOOR_ADHESION_AD_SELECTOR = "#ext-wikia-adEngine-template-footer .ad";
    private final String
            FLOOR_ADHESION_CLOSE_SELECTOR = "#ext-wikia-adEngine-template-footer .close";
    private final String WIKIA_BAR_SELECTOR = "#WikiaBar";

    @Test(
            dataProvider = "floorAdhesionOnOasis",
            dataProviderClass = AdsDataProvider.class,
            groups = {"TestFloorAdhesionOasis"}
    )
    public void testFloorAdhesionOnOasis(
            String wikiName,
            String article,
            String floorAdhesionModalSelector,
            String floorAdhesionModalCloseSelector
    ) {
        String testPage = urlBuilder.getUrlForPath(wikiName, article);
        AdsBaseObject wikiPage = new AdsBaseObject(driver, testPage);

        // verify the unit is visible
        wikiPage.waitForElementByCss(FLOOR_ADHESION_SELECTOR);
        PageObjectLogging.log(
                "testFloorAdhesionOnOasis",
                "Floor Adhesion unit visible",
                true
        );

        // verify there is no Wikia Bar
        WebElement wikiaBar = driver.findElement(By.cssSelector(WIKIA_BAR_SELECTOR));
        wikiPage.waitForElementNotVisibleByElement(wikiaBar);
        PageObjectLogging.log(
                "testFloorAdhesionOnOasis",
                "WikiaBar not visible",
                true
        );

        // verify clicking it opens lightbox
        driver.findElement(By.cssSelector(FLOOR_ADHESION_AD_SELECTOR)).click();
        wikiPage.waitForElementByCss(floorAdhesionModalSelector);

        // verify closing modal and closing Floor Adhesion unit works
        driver.findElement(By.cssSelector(floorAdhesionModalCloseSelector)).click();
        driver.findElement(By.cssSelector(FLOOR_ADHESION_CLOSE_SELECTOR)).click();
    }

    @Test(
            dataProvider = "floorAdhesionOnMercury",
            dataProviderClass = MobileAdsDataProvider.class,
            groups = {"TestFloorAdhesionMercury", "MercuryAds"}
    )
    public void floorAdhesionOnMercury(
            String wikiName,
            String article,
            String floorAdhesionModalSelector,
            String floorAdhesionModalCloseSelector
    ) {
        String testPage = urlBuilder.getUrlForPath(wikiName, article);
        MobileAdsBaseObject wikiPage = new MobileAdsBaseObject(driver, testPage);

        // verify the unit is visible
        wikiPage.waitForElementByCss(FLOOR_ADHESION_SELECTOR);
        PageObjectLogging.log(
                "testFloorAdhesionOnOasis",
                "Floor Adhesion unit visible",
                true
        );

        // verify clicking it opens lightbox
        driver.findElement(By.cssSelector(FLOOR_ADHESION_AD_SELECTOR)).click();
        wikiPage.waitForElementByCss(floorAdhesionModalSelector);

        // verify closing modal and closing Floor Adhesion unit works
        driver.findElement(By.cssSelector(floorAdhesionModalCloseSelector)).click();
        driver.findElement(By.cssSelector(FLOOR_ADHESION_CLOSE_SELECTOR)).click();
    }
}
