package onlyPack;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class MainClass {
	
	static WebDriver driver;
	static List<WebElement> tempList;
	static WebElement temp;

	public static void main(String[] args) throws InterruptedException {
		System.setProperty("webdriver.chrome.driver", "c:/Studies/Java/selenium-2.53.0/chromedriver.exe");
		driver = new ChromeDriver();
		login();
		getListings();
		driver.close();
		driver.quit();

	}
	private static void login() throws InterruptedException {
		driver.get("https://www.tailster.com/login");
		temp = driver.findElement(By.id("emailLogin"));
		temp.click();
		temp.clear();
		temp.sendKeys("tmsldck@gmail.com");
		temp = driver.findElement(By.id("passwordLogin"));
		temp.click();
		temp.clear();
		temp.sendKeys("toni1088");
		Thread.sleep(5000);
		temp = driver.findElement(By.className("prepend-40"));
		temp.submit();
		Thread.sleep(5000);
	}
	private static void getListings() throws InterruptedException {
		double distance;
		String type;
		ArrayList<String> listingsToApply = new ArrayList<String>();
		driver.get("https://www.tailster.com/jobs/nearme");
		Thread.sleep(5000);
		temp = driver.findElement(By.className("tablesaw"));
		tempList = temp.findElements(By.className("repeat-animation"));
		for (int i=0; i<tempList.size(); i++) {
			temp = tempList.get(i);
			if (!temp.findElement(By.className("applied")).getText().trim().contentEquals("No")) continue;
			type = temp.findElement(By.className("small")).getText().trim();
			type = type.substring(0, type.indexOf('m')).trim();
			distance = Double.parseDouble(type);
			type = temp.findElement(By.className("title")).findElement(By.tagName("a")).getAttribute("href");
			if (distance < 3.0) listingsToApply.add(type);
			else {
				if ((type.contains("sitting")) || (type.contains("boarding"))) listingsToApply.add(type);
			}
		}
		for (int i=0; i<listingsToApply.size(); i++) {
			System.out.println(listingsToApply.get(i));
			quote(listingsToApply.get(i));
		}
	}
	private static void quote(String url) {
		WebDriverWait wait = new WebDriverWait(driver, 10);
		String datesString;
		int walkPerWeek = 0;
		int numOfNights = 0;
		int numOfDaysDayCare = 0;
		int numOfPets;
		int quote;
		driver.get(url);
		temp = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("job-info")));
		datesString = temp.getText();
		String[] lines = datesString.split("\n");
		for (int i=0; i<lines.length; i++) {
			if (lines[i].startsWith("Dates:")) datesString = lines[i];
			if (lines[i].startsWith("Walks per week:")) datesString = lines[i];
			if (lines[i].startsWith("Days per week:")) datesString = lines[i];
		}
		datesString = datesString.trim().substring(datesString.indexOf(':')+2);
		//if dateString is more than 1 character it is boarding or sitting
		if (datesString.length() < 2) walkPerWeek = Integer.parseInt(datesString.trim());
		else {
			//Sep 2016 - 8 night(s)
			if (datesString.contains("night")) {
				datesString = datesString.substring(datesString.indexOf('-')+1, datesString.indexOf('n'));
				numOfNights = Integer.parseInt(datesString.trim());
			}
			//5 days
			else if (datesString.contains("days")) {
				datesString = datesString.substring(0, datesString.indexOf('d')-1);
				numOfDaysDayCare = Integer.parseInt(datesString.trim());
			}
			//Aug 8, 2016 - Aug 15, 2016
			else {
				SimpleDateFormat myFormat = new SimpleDateFormat("MMM d, yyyy");
				try {
					Date date1 = myFormat.parse(datesString.substring(0, datesString.indexOf('-')-1));
					Date date2 = myFormat.parse(datesString.substring(datesString.indexOf('-')+2));
					long diff = date2.getTime() - date1.getTime();
					numOfNights = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
					if (numOfNights == 0) numOfNights = 1;
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (StringIndexOutOfBoundsException sioobe) {
					numOfNights = 5;
				}
				
			}
		}
		try {
			driver.findElement(By.className("job-single-dog-wrap"));
			numOfPets = 1;
		}
		catch (NoSuchElementException nsee) {
			temp = driver.findElement(By.className("multiple-dogs"));
			numOfPets = temp.findElements(By.tagName("a")).size();
		}
		System.out.println(numOfPets);
		System.out.println(numOfNights);
		System.out.println(walkPerWeek);
		
		if (walkPerWeek > 0) {
			quote = numOfPets*7*walkPerWeek;
		}
		else if (numOfDaysDayCare > 0) {
			quote = numOfPets*15*numOfDaysDayCare;
		}
		else {
			quote = numOfPets*15*numOfNights;
		}
		temp = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("form-control")));
		temp.click();
		temp.clear();
		temp.sendKeys(quote+".00");
		
		tempList = driver.findElements(By.className("btn"));
		for (int i=0; i<tempList.size(); i++) {
			datesString = tempList.get(i).getAttribute("data-ng-click");
			if (datesString != null && datesString.contains("quoted=true")) {
				tempList.get(i).click();
				break;
			}
		}
	}

}
