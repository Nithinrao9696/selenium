package com.wikia.webdriver.elements.communities.mobile.components.discussions.mobile;

import com.wikia.webdriver.pageobjectsfactory.pageobject.BasePageObject;

import lombok.Getter;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class DiscussionsHeader extends BasePageObject {

  @Getter(lazy = true)
  private final FiltersPopOverMobile filtersPopOverMobile = new FiltersPopOverMobile();
  @FindBy(css = ".discussion-mobile-filter > .wds-dropdown__toggle")
  private WebElement filterMenuButton;

  public FiltersPopOverMobile openFilterMenu() {
    waitAndClick(filterMenuButton);
    return this.getFiltersPopOverMobile();
  }
}
