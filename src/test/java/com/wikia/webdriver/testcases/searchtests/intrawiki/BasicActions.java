package com.wikia.webdriver.testcases.searchtests.intrawiki;

import com.wikia.webdriver.common.core.urlbuilder.UrlBuilder;
import com.wikia.webdriver.common.dataprovider.IntraWikiSearchProvider;
import com.wikia.webdriver.common.properties.Credentials;
import com.wikia.webdriver.common.templates.NewTestTemplate;
import com.wikia.webdriver.pageobjectsfactory.componentobject.global_navitagtion.NavigationBar;
import com.wikia.webdriver.pageobjectsfactory.pageobject.SearchPageObject;
import com.wikia.webdriver.pageobjectsfactory.pageobject.WikiBasePageObject;
import com.wikia.webdriver.pageobjectsfactory.pageobject.search.intrawikisearch.IntraWikiSearchPageObject;
import com.wikia.webdriver.pageobjectsfactory.pageobject.search.intrawikisearch.IntraWikiSearchPageObject.sortOptions;

import org.testng.annotations.Test;

import java.util.List;

/*
 *  anonSearch: As anon basic search action and verify you are on search result page. This also prevents goSearch being active by default.
 *  userSearch: As user do basic search action and verify you are on search result page. This also prevents goSearch being active by default.
 *  1. Search for different phrases and verify if they give correct first result
 *  2. pagination:  Check search page pagination
 *  3. resultsCount: Verify number of results on page
 *  4. noResults: Search for not existing phrase and verify there is no results
 *  5. filtering: Search for some phrase and verify filtering options work correctly and give correct results
 *  6. sortingVideos: Search for some phrase and verify sorting options for video give correct results
 *  7. sortingImages: Search for some phrase and verify sorting options for images give correct results
 *  9. Verify search page hubs and titles are translatable
 *  10. Select photos only option and verify there are only photos,
 *		then select videos only option and verify:
 * 			1. the number of videos = 25
 *			2. the number of videos equals the number of play buttons
 *			3. video titles start with "file" prefix
 *  11. Verify if there are correct advanced option set as a default
 *  12. Search for some image without typing extension (.jpg) and verify photo is found
 *  13. Search for different phrases and verify there are correct namespaces in result titles
 *  14. Search for empty field and verify if search page is opened
 *  15. Verify top module
 *  16. Verify push to top is working in community.wikia.com
 */

/**
 * @author  
 */
public class BasicActions extends NewTestTemplate {

    protected Credentials credentials = config.getCredentials();

    protected String testedWiki;
    protected String communityWiki;
    protected String searchSuggestionsWiki;

    public BasicActions() {
        UrlBuilder urlBuilder = new UrlBuilder(config.getEnv());
        testedWiki = urlBuilder.getUrlForWiki("muppet");
        communityWiki = urlBuilder.getUrlForWiki("community");
        searchSuggestionsWiki = urlBuilder.getUrlForWiki("communitycouncil");
    }

    protected static final int RESULTS_PER_PAGE = 25;
    protected static final String SEARCH_PHRASE_RESULTS = "a";
    protected static final String SEARCH_PAGINATION_RESULTS = "what";
    protected static final String SEARCH_RESULT_WITH_EXTENSION = "betweenlions";
    protected static final String SEARCH_PHRASE_NO_RESULTS = "qazwsxedcrfvtgb";
    protected static final String SEARCH_SUGGESTION_PHRASE = "Gon";
    protected static final String SEARCH_ARTICLE = "Gonzo";
    protected static final String SEARCH_WIKI = "Marvel";

    @Test(groups = { "anonSearch", "IntraWikiSearch", "Search" })
    public void anonSearch() {
        WikiBasePageObject base = new WikiBasePageObject(driver);
        base.openWikiPage(testedWiki);
        NavigationBar navigation = new NavigationBar(driver);
        IntraWikiSearchPageObject search = navigation
                .searchFor(SEARCH_PHRASE_RESULTS);
        search.verifyFirstArticleNameTheSame(SEARCH_PHRASE_RESULTS);
    }

    @Test(groups = { "userSearch", "IntraWikiSearch", "Search" })
    public void userSearch() {
        WikiBasePageObject base = new WikiBasePageObject(driver);
        base.openWikiPage(testedWiki);
        base.logInCookie(credentials.userName, credentials.password, wikiURL);
        NavigationBar navigation = new NavigationBar(driver);
        IntraWikiSearchPageObject search = navigation
                .searchFor(SEARCH_PHRASE_RESULTS);
        search.verifyFirstArticleNameTheSame(SEARCH_PHRASE_RESULTS);
    }

    @Test(groups = { "IntraWikiSearch_002", "IntraWikiSearch", "Search" })
    public void pagination() {
        IntraWikiSearchPageObject search = new IntraWikiSearchPageObject(driver);
        search.openWikiPage(testedWiki);
        search.searchFor(SEARCH_PAGINATION_RESULTS);
        String firstResult = search.getTitleInnerText();
        search.verifyPagination();
        search.clickNextPaginator();
        search.verifyFirstArticleNameNotTheSame(firstResult);
        search.verifyPagination();
        search.clickPrevPaginator();
        search.verifyFirstArticleNameTheSame(firstResult);
        search.verifyPagination();
        search.verifyLastResultPage();
    }

    @Test(groups = { "IntraWikiSearch_003", "IntraWikiSearch", "Search" })
    public void resultsCount() {
        IntraWikiSearchPageObject search = new IntraWikiSearchPageObject(driver);
        search.openWikiPage(testedWiki);
        search.searchFor(SEARCH_PHRASE_RESULTS);
        search.verifyResultsCount(RESULTS_PER_PAGE);
        search.clickNextPaginator();
        search.verifyResultsCount(RESULTS_PER_PAGE);
    }

    @Test(groups = { "IntraWikiSearch_004", "IntraWikiSearch", "Search" })
    public void noResults() {
        IntraWikiSearchPageObject search = new IntraWikiSearchPageObject(driver);
        search.openWikiPage(testedWiki);
        search.searchFor(SEARCH_PHRASE_NO_RESULTS);
        search.verifyNoResults();
    }

    @Test(groups = { "IntraWikiSearch_005", "IntraWikiSearch", "Search" })
    public void filtering() {
        IntraWikiSearchPageObject search = new IntraWikiSearchPageObject(driver);
        search.openWikiPage(testedWiki);
        search.searchFor(SEARCH_PHRASE_RESULTS);
        search.selectPhotosVideos();
        search.verifyTitlesNotEmpty();
        search.selectPhotosOnly();
        search.verifyTitlesNotEmpty();
        search.verifyAllResultsImages(RESULTS_PER_PAGE);
        search.selectVideosOnly();
        search.verifyTitlesNotEmpty();
        search.verifyAllResultsVideos(RESULTS_PER_PAGE);
    }

    @Test(groups = { "IntraWikiSearch_006", "IntraWikiSearch", "Search" })
    public void sortingVideos() {
        IntraWikiSearchPageObject search = new IntraWikiSearchPageObject(driver);
        search.openWikiPage(testedWiki);
        search.searchFor(SEARCH_PHRASE_RESULTS);
        search.selectPhotosVideos();
        search.selectVideosOnly();
        search.verifyTitlesNotEmpty();
        search.sortBy(sortOptions.DURATION);
        List<String> titles1 = search.getTitles();
        search.sortBy(sortOptions.RELEVANCY);
        List<String> titles2 = search.getTitles();
        search.sortBy(sortOptions.PUBLISH_DATE);
        List<String> titles3 = search.getTitles();
        search.compareTitleListsNotEquals(titles1, titles2);
        search.compareTitleListsNotEquals(titles1, titles3);
        search.compareTitleListsNotEquals(titles2, titles3);
    }

    @Test(groups = { "IntraWikiSearch_007", "IntraWikiSearch", "Search" })
    public void sortingImages() {
        IntraWikiSearchPageObject search = new IntraWikiSearchPageObject(driver);
        search.openWikiPage(testedWiki);
        search.searchFor(SEARCH_PHRASE_RESULTS);
        search.selectPhotosVideos();
        search.selectPhotosOnly();
        search.verifyTitlesNotEmpty();
        search.sortBy(sortOptions.RELEVANCY);
        List<String> titles1 = search.getTitles();
        search.sortBy(sortOptions.PUBLISH_DATE);
        List<String> titles2 = search.getTitles();
        search.compareTitleListsNotEquals(titles1, titles2);
    }

    @Test(groups = { "IntraWikiSearch_009", "IntraWikiSearch", "Search" })
    public void languageTranslation() {
        IntraWikiSearchPageObject search = new IntraWikiSearchPageObject(driver);
        search.openWikiPage(testedWiki);
        search.searchFor(SEARCH_PHRASE_RESULTS);
        search.addQqxUselang();
        search.verifyLanguageTranslation();
    }

    @Test(groups = { "IntraWikiSearch_010", "IntraWikiSearch", "Search" })
    public void selectImagesOrVideos() {
        IntraWikiSearchPageObject search = new IntraWikiSearchPageObject(driver);
        search.openWikiPage(testedWiki);
        search.searchFor(SEARCH_PHRASE_RESULTS);
        search.selectPhotosVideos();
        search.selectPhotosOnly();
        search.verifyPhotosOnly();
        search.selectVideosOnly();
        search.verifyVideosOnly();
    }

    @Test(groups = { "IntraWikiSearch_011", "IntraWikiSearch", "Search" })
    public void defaultNamespaces() {
        IntraWikiSearchPageObject search = new IntraWikiSearchPageObject(driver);
        search.openWikiPage(testedWiki);
        search.searchFor(SEARCH_PHRASE_RESULTS);
        search.clickAdvancedButton();
        search.verifyDefaultNamespaces();
    }

    @Test(groups = { "IntraWikiSearch_012", "IntraWikiSearch", "Search" })
    public void noFileExtensionNeed() {
        IntraWikiSearchPageObject search = new IntraWikiSearchPageObject(driver);
        search.openWikiPage(testedWiki);
        search.searchFor(SEARCH_RESULT_WITH_EXTENSION);
        search.selectPhotosVideos();
        search.verifyFirstResultExtension(SEARCH_RESULT_WITH_EXTENSION);
    }

    @Test(dataProviderClass = IntraWikiSearchProvider.class, dataProvider = "getNamespaces", groups = {
            "IntraWikiSearch_013", "IntraWikiSearch", "Search" })
    public void namespaces(String searchPhrase, String namespace) {
        IntraWikiSearchPageObject search = new IntraWikiSearchPageObject(driver);
        search.openWikiPage(testedWiki);
        search.searchFor(searchPhrase);
        search.selectAllAdvancedOptions();
        SearchPageObject searchPage = new SearchPageObject(driver);
        searchPage.clickSearchButton();
        searchPage.setSearchTab(SearchPageObject.SearchTab.EVERYTHING);
        search.verifyNamespace(namespace);
    }

    @Test(groups = { "IntraWikiSearch_014", "IntraWikiSearch", "Search" })
    public void searchPageOpened() {
        IntraWikiSearchPageObject search = new IntraWikiSearchPageObject(driver);
        search.openWikiPage(testedWiki);
        search.searchFor("");
        search.verifySearchPageOpened();
    }

    @Test(groups = { "IntraWikiSearch_015", "IntraWikiSearch", "Search" })
    public void topModule() {
        IntraWikiSearchPageObject search = new IntraWikiSearchPageObject(driver);
        search.openWikiPage(testedWiki);
        search.searchFor(SEARCH_PHRASE_RESULTS);
        search.verifyTopModule();
    }

    @Test(groups = { "IntraWikiSearch_016", "IntraWikiSearch", "Search" })
    public void communityPushToTopWikiResult() {
        IntraWikiSearchPageObject search = new IntraWikiSearchPageObject(driver);
        search.openWikiPage(communityWiki);
        search.searchForInGlobalNavIfPresent(SEARCH_WIKI);
        search.verifyPushToTopWikiTitle(SEARCH_WIKI);
        search.verifyPushToTopWikiThumbnail();
    }

}
