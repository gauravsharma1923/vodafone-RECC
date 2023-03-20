package recc;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String strDB_URL="ussd-scan3.vodafone.in:1610/USSD";
		String servicename = (String) strDB_URL.substring(strDB_URL
				.lastIndexOf("/") + 1);
		System.out.println("hii="+servicename);

	}

}
