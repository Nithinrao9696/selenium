package com.wikia.webdriver.PageObjectsFactory.PageObject.AdsBase;

import com.wikia.webdriver.Common.ContentPatterns.AdsContent;
import com.wikia.webdriver.Common.Core.Assertion;
import com.wikia.webdriver.Common.Core.ImageUtilities.Shooter;
import com.wikia.webdriver.Common.Core.NetworkTrafficInterceptor.NetworkTrafficInterceptor;
import com.wikia.webdriver.Common.Logging.PageObjectLogging;
import com.wikia.webdriver.PageObjectsFactory.PageObject.AdsBase.Helpers.AdsComparison;
import com.wikia.webdriver.PageObjectsFactory.PageObject.WikiBasePageObject;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * @author Bogna 'bognix' Knychala
 */
public class AdsBaseObject extends WikiBasePageObject {

	private final String  wikiaMessageBuble = "#WikiaNotifications div[id*='msg']";
	private final String liftiumIframeSelector = "iframe[id*='Liftium']";

	@FindBy(css=AdsContent.wikiaBarSelector)
	private WebElement toolbar;
	@FindBy(css="#WikiaPage")
	private WebElement wikiaArticle;
	@FindBy(css=".WikiaSpotlight")
	private List<WebElement> spotlights;
	@FindBy(css=liftiumIframeSelector)
	private List<WebElement> liftiumIframes;
	@FindBy(css="div[id*='TOP_LEADERBOARD']")
	protected WebElement presentLeaderboard;
	@FindBy(css="div[id*='TOP_RIGHT_BOXAD']")
	protected WebElement presentMedrec;

	protected NetworkTrafficInterceptor networkTrafficInterceptor;
	protected String presentLeaderboardName;
	protected String presentLeaderboardSelector;
	protected String presentMedrecName;
	protected String presentMedrecSelector;

	public AdsBaseObject(WebDriver driver, String page) {
		super(driver);
		AdsContent.setSlotsSelectors();
		getUrl(page);
		setSlots();
	}

	public AdsBaseObject(
		WebDriver driver, String page,
		NetworkTrafficInterceptor networkTrafficInterceptor
	) {
		super(driver);
		AdsContent.setSlotsSelectors();
		networkTrafficInterceptor.startIntercepting(page);
		getUrl(page);
		this.networkTrafficInterceptor = networkTrafficInterceptor;
		setSlots();
	}

	public AdsBaseObject(WebDriver driver) {
		super(driver);
	}

	public AdsBaseObject(WebDriver driver, String testedPage, Dimension resolution) {
		super(driver);
		driver.manage().window().setSize(resolution);
		getUrl(testedPage);
	}

	private void setSlots() {
		if (checkIfElementOnPage(presentLeaderboard)) {
			presentLeaderboardName = presentLeaderboard.getAttribute("id");
			presentLeaderboardSelector = "#" + presentLeaderboardName;
		} else {
			presentLeaderboardName = null;
			presentLeaderboardSelector = null;
			presentLeaderboard = null;
		}
		if (checkIfElementOnPage(presentMedrec)) {
			presentMedrecName = presentMedrec.getAttribute("id");
			presentMedrecSelector = "#" + presentMedrecName;
		} else {
			presentMedrec = null;
			presentMedrecName = null;
			presentMedrecSelector = null;
		}
	}

	public void verifyRoadblockServedAfterMultiplePageViews(
		String page, String adSkinUrl, Dimension windowResolution, int skinWidth,
		String expectedAdSkinLeftPart, String expectedAdSkinRightPart, int numberOfPageViews
	) {
		setSlots();
		String leaderboardAd = getSlotImageAd(presentLeaderboard);
		String medrecAd = getSlotImageAd(presentMedrec);
		verifyAdSkinPresenceOnGivenResolution(
			page,
			adSkinUrl,
			windowResolution,
			skinWidth,
			expectedAdSkinLeftPart,
			expectedAdSkinRightPart
		);

		for (int i=0; i <= numberOfPageViews; i++) {
			refreshPage();
			verifyAdSkinPresenceOnGivenResolution(
				page,
				adSkinUrl,
				windowResolution,
				skinWidth,
				expectedAdSkinLeftPart,
				expectedAdSkinRightPart
			);
			Assertion.assertEquals(leaderboardAd, getSlotImageAd(presentLeaderboard));
			Assertion.assertEquals(medrecAd, getSlotImageAd(presentMedrec));
		}
	}

	public void verifyForcedSuccessScriptInSlots(List<String> slots) {
		for (String slot : slots) {
			WebElement slotElement = driver.findElement(By.id(slot));
			WebElement slotGptIframe = slotElement.findElement(By.cssSelector("div > iframe"));
			driver.switchTo().frame(slotGptIframe);
			List<WebElement> scriptsInFrame = driver.findElements(By.tagName("script"));
			String adDriverForcedSuccessFormatted = String.format(
				AdsContent.adDriverForcedStatusSuccessScript, slot
			);
			if (checkIfScriptInsideScripts(scriptsInFrame, adDriverForcedSuccessFormatted)) {
				PageObjectLogging.log(
					"AdDriver2ForceStatus script",
					"adDriverForcedSuccess script found in slot " + slot,
					true
				);
			} else {
				throw new NoSuchElementException(
					"AdDriver2ForcedStatus script not found in slot " + slot
				);
			}
			driver.switchTo().defaultContent();
		}
	}
	public void checkMedrec() {
		checkAdVisibleInSlot(presentMedrecSelector, presentMedrec);
	}

	public void checkTopLeaderboard() {
		checkAdVisibleInSlot(presentLeaderboardSelector, presentLeaderboard);
	}

	protected void checkAdVisibleInSlot(String slotSelector, WebElement slot ) {
		AdsComparison adsComparison = new AdsComparison();
		extractLiftiumTagId(slotSelector);
		boolean result = adsComparison.compareSlotOnOff(
			slot, slotSelector, driver
		);
		if(result) {
			PageObjectLogging.log(
				"CompareScreenshot", "Screenshots look the same", false
			);
			throw new NoSuchElementException(
				"Screenshots of element on/off look the same."
				+ "Most probable ad is not present; CSS "
				+ slotSelector
			);
		} else {
			PageObjectLogging.log(
				"CompareScreenshot", "Screenshots are different", true
			);
		}
	}

	/**
	 * Check Ad skin on page with provided resolution.
	 * Compare left and right sides of skin with provided Base64.
	 * @param page - url
	 * @param adSkinUrl - DFP link with ad skin image
	 * @param windowResolution - resolution
	 * @param skinWidth - skin width on the sides of the article
	 * @param expectedAdSkinLeftPart - path to file with expected skin encoded in Base64
	 * @param expectedAdSkinRightPart - path to file with expected skin encoded in Base64
	 */
	public void verifyAdSkinPresenceOnGivenResolution(
		String page, String adSkinUrl, Dimension windowResolution, int skinWidth,
		String expectedAdSkinLeftPart, String expectedAdSkinRightPart
	) {
		Shooter shooter = new Shooter();
		AdsContent.setSlotsSelectors();

		String backgroundImageUrlAfter = getPseudoElementValue(
			body, ":after", "backgroundImage"
		);
		Assertion.assertStringContains(backgroundImageUrlAfter, adSkinUrl);

		String backgroundImageUrlBefore = getPseudoElementValue(
			body, ":before", "backgroundImage"
		);
		Assertion.assertStringContains(backgroundImageUrlBefore, adSkinUrl);

		PageObjectLogging.log(
			"ScreenshotPage",
			"Screenshot of the page taken",
			true, driver
		);

		AdsComparison adsComparison = new AdsComparison();
		adsComparison.hideSlot(AdsContent.getSlotSelector(AdsContent.wikiaBar), driver);
		hideMessage();

		int articleLocationX = wikiaArticle.getLocation().x;
		int articleWidth = wikiaArticle.getSize().width;
		Point articleLeftSideStartPoint = new Point(articleLocationX - skinWidth,100);
		Point articleRightSideStartPoint = new Point(articleLocationX + articleWidth,100);
		Dimension skinSize = new Dimension(skinWidth, 500);

		boolean successLeft = adsComparison.compareImageWithScreenshot(
			expectedAdSkinLeftPart, skinSize, articleLeftSideStartPoint, driver
		);
		if (successLeft) {
			PageObjectLogging.log(
				"ExpectedSkinFound",
				"Expected ad skin found on page - left side of skin",
				true
			);
		} else {
			PageObjectLogging.log(
				"ExpectedSkinNotFound",
				"Expected ad skin not found on page - left side of skin",
				false, driver
			);
		}

		boolean successRight = adsComparison.compareImageWithScreenshot(
			expectedAdSkinRightPart, skinSize, articleRightSideStartPoint, driver
		);
		if (successRight) {
			PageObjectLogging.log(
				"ExpectedSkinFound",
				"Expected ad skin found on page - right side of skin",
				true
			);
		} else {
			PageObjectLogging.log(
				"ExpectedSkinNotFound",
				"Expected ad skin not found on page - right side of skin",
				false, driver
			);
		}

		if (!successLeft || !successRight) {
			throw new NoSuchElementException("Skin not found on page");
		}
	}

	private void hideMessage() {
		if (checkIfElementOnPage(wikiaMessageBuble)) {
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("$(arguments[0]).css('visibility', 'hidden')", wikiaMessageBuble);
		}
	}

	public void verifyHubTopLeaderboard() throws Exception {
		String hubLBName = AdsContent.hubLB;
		WebElement hubLB = driver.findElement(By.cssSelector(AdsContent.getSlotSelector(hubLBName)));
		checkScriptPresentInSlotScripts(hubLBName, hubLB);
		PageObjectLogging.log("HUB_TOP_LEADERBOARD found", "HUB_TOP_LEADERBOARD found", true);

		WebElement hubGPT_LB = hubLB.findElement(By.cssSelector(AdsContent.getSlotSelector(AdsContent.hubLB_gpt)));
		PageObjectLogging.log("HUB_TOP_LEADERBOARD_gpt found", "HUB_TOP_LEADERBOARD_gpt found", true);

		if(hubGPT_LB.findElements(By.cssSelector("iframe")).size() > 1) {
			PageObjectLogging.log("IFrames found", "2 IFrames found in HUB_TOP_LEADERBOAD_gpt div", true);
		} else {
			PageObjectLogging.log(
				"IFrames not found",
				"2 IFrames expected to be found in HUB_TOP_LEADERBOAD_gpt div, found less",
				false, driver
			);
			throw new Exception("IFrames inside GPT div not found!");
		}
	}

	public void verifyNoLiftiumAdsOnPage() {
		scrollToSelector(AdsContent.getSlotSelector("AdsInContent"));
		scrollToSelector(AdsContent.getSlotSelector("Prefooters"));
		verifyNoLiftiumAds();
	}

	public void verifyNoAdsOnPage() {
		scrollToSelector(AdsContent.getSlotSelector("AdsInContent"));
		scrollToSelector(AdsContent.getSlotSelector("Prefooters"));
		verifyNoAds();
	}

	private boolean checkTagsPresent(WebElement slotElement) {
		try {
			waitForOneOfTagsPresentInElement(slotElement, "img", "iframe");
			PageObjectLogging.log(
				"IFrameOrImageFound",
				"Image or iframe was found in slot in less then 30 seconds",
				true,
				driver
			);
			return true;
		} catch (TimeoutException e) {
			PageObjectLogging.log(
				"IFrameOrImgNotFound",
				"Nor image or iframe was found in slot for 30 seconds",
				false,
				driver
			);
			return false;
		}
	}

	private boolean checkScriptPresentInSlot(WebElement slot, String script) {
		List<WebElement> scriptsTags = slot.findElements(By.tagName("script"));
		return checkIfScriptInsideScripts(scriptsTags, script);
	}

	protected boolean checkIfScriptInsideScripts(List<WebElement> scripts, String script) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		for (WebElement scriptNode : scripts) {
			String result = (String) js.executeScript(
				"return arguments[0].innerHTML", scriptNode
			);
			String trimedResult = result.replaceAll("\\s", "");
			if (trimedResult.contains(script)) {
				return true;
			}
		}
		return false;
	}

	private boolean checkScriptPresentInSlotScripts(String slotName, WebElement slotElement) throws Exception {
		String scriptExpectedResult = AdsContent.adsPushSlotScript.replace(
			"%slot%", slotName
		);
		boolean scriptFound = checkScriptPresentInSlot(slotElement, scriptExpectedResult);
		if (scriptFound) {
			PageObjectLogging.log(
				"PushSlotsScriptFound",
				"Script " + scriptExpectedResult + " found",
				true
			);
		} else {
			PageObjectLogging.log(
				"PushSlotsScriptNotFound",
				"Script " + scriptExpectedResult + " not found",
				false,
				driver
			);
			throw new Exception("Script for pushing ads not found in element");
		}
		return scriptFound;
	}

	private void verifyNoAds() {
		Collection<String> slotsSelectors = AdsContent.slotsSelectors.values();
		for (String selector: slotsSelectors) {
			if (checkIfElementOnPage(selector)) {
				WebElement element = driver.findElement(By.cssSelector(selector));
				if (
					element.isDisplayed()
					&& element.getSize().getHeight() > 1
					&& element.getSize().getWidth() > 1
				) {
					throw new WebDriverException("Ads found on page");
				} else {
					PageObjectLogging.log(
						"AdsFoundButNotVisible",
						"Ads found on page with selector: "
						+ selector
						+ " but is smaller then 1x1 or hidden",
						true
					);
				}
			} else {
				PageObjectLogging.log(
					"AdNotFound",
					"Ad with selector: "
					+ selector
					+ " not found on page",
					true
				);
			}
		}
	}

	protected void verifyAdsFromProvider(String providerName, List<WebElement> slots) {
		String providerSpecificSelector = AdsContent.getElementForProvider(providerName);
		for (WebElement slot: slots) {
			if (!checkIfElementInElement(providerSpecificSelector, slot)) {
				PageObjectLogging.log(
					"NoAdsFromProvider",
					"Ads from " + providerName
					+ " not found in slot: " + slot.getAttribute("id"),
					false
				);
				throw new NoSuchElementException(
					"Call to provider: " + providerName
					+ " in slot: " + slot.getAttribute("id") + " not found!"
				);
			}
			PageObjectLogging.log(
				"AdsFromProviderFound",
				"Ads from " + providerName
				+ " found in slot: " + slot.getAttribute("id"),
				true
			);
		}
	}

	private void verifyNoLiftiumAds() {
		if (checkIfElementOnPage(liftiumIframeSelector)) {
			throw new WebDriverException("Liftium ads found!");
		} else {
			PageObjectLogging.log("LiftiumAdsNotFound", "Liftium ads not found", true);
		}
	}

	private String extractLiftiumTagId(String slotSelector) {
		String liftiumTagId = null;
		WebElement slot = driver.findElement(By.cssSelector(slotSelector));
		if (checkIfElementInElement(liftiumIframeSelector, slot)) {
			JavascriptExecutor js = (JavascriptExecutor)driver;
			WebElement currentLiftiumIframe = (WebElement)js.executeScript(
				"return $(arguments[0] + ' iframe[id*=\\'Liftium\\']:visible')[0];",
				slotSelector
			);
			String liftiumAdSrc = currentLiftiumIframe.getAttribute("src");
			Pattern pattern = Pattern.compile("tag_id=\\d*");
			Matcher matcher = pattern.matcher(liftiumAdSrc);
			if (matcher.find()) {
				liftiumTagId = matcher.group().replaceAll("[^\\d]", "");
			}
		}

		if (liftiumTagId != null) {
			PageObjectLogging.log(
				"LiftiumTagId",
				"Present liftium tag id is: "
				+ liftiumTagId + "; in slot: " + slotSelector,
				true
			);
		} else {
			PageObjectLogging.log(
				"LiftiumTagId",
				"Liftium not present in slot: " + slotSelector,
				true
			);
		}

		return liftiumTagId;
	}

	protected String getSlotImageAd(WebElement slot) {
		WebElement iframeWithAd = slot.findElement(
			By.cssSelector("div > iframe:not([id*='hidden'])")
		);
		driver.switchTo().frame(iframeWithAd);
		String imageAd = driver.findElement(
			By.cssSelector("img")
		).getAttribute("src");
		driver.switchTo().defaultContent();
		return imageAd;
	}

	protected boolean checkIfSlotExpanded(WebElement slot) {
		return slot.getSize().getHeight() > 1 && slot.getSize().getWidth() > 1;
	}

	public void verifyNoLiftiumAdsInSlots(List<String> slots) {
		for (String slot : slots) {
			WebElement slotElement = driver.findElement(By.id(slot));
			if (checkIfElementInElement(liftiumIframeSelector, slotElement)) {
				throw new NoSuchElementException("Liftium found in slot " + slot);
			} else {
				PageObjectLogging.log("LiftiumNotFound", "Liftium not found in slot " + slot, true);
			}
		}
	}
}
