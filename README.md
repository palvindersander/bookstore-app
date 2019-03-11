<a href="https://github.com/palvinderssingh/BookStoreApp"><img src="https://raw.githubusercontent.com/palvinderssingh/BookStoreApp/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" title="BookStoreApp" alt="Palvinder Singh Sander"></a>
# bookstore Android Application
[![Build Status](http://img.shields.io/travis/badges/badgerbadgerbadger.svg?style=flat-square)](https://github.com/palvinderssingh/BookStoreApp)
> Android Application that principally provides a platform for users to share books, within a school community
> A-Level computer science programming project
## Features
Many students have spare books at home that will not be used again; these books
could be rented to others. A utility that conveniently allows for this exchange does
not exist for three primary reasons, it must be: intuitive, secure and fair. To overcome
these issues, I am developing features that allows users to: create an account, add
their books by scanning them or otherwise, search for and request a book to rent,
message users to plan for the exchange and rate others based on their quality. This
will come hand in hand with rigorous policies; these will ensure that the user has an
incentive to add more books and be compliant with book return deadlines.
## Documentation
- Development documentation <a href="https://github.com/palvinderssingh/BookStoreWriteUp" target="_blank">here</a>
## Phatt Tasks
### How I Get ISBN Data
Given an ISBN use this class to fetch data about a book. This data can include title, author, image, genre etc. IF using this class in an android application it must be instantiated from within an AsyncTask class (due to network use on the main thread); official docs <a href="https://developer.android.com/reference/android/os/AsyncTask">here</a>.
```java
class bookMetaData {
	  private String isbn;
	  private HashMap<String, String> mapData;

	  bookMetaData(String isbn) {
		  this.isbn = isbn;
		  this.mapData = getData();
	 }

	  private HashMap getData() {
		  JSONObject JSONData = getJSONData();
		  JSONObject volumeInfo = getVolumeInfo(JSONData);
		  if (volumeInfo == null) {
			  return null;
		 } else {
			  String title = getBookTitle(volumeInfo);
			  String image = getImageURl(volumeInfo);
			  HashMap<String, String> data = new HashMap<>();
			  data.put("title", title);
			  data.put("image", image);
			  return data;
		 }
	 }

	  private JSONObject getJSONData() {
		  try {
			  URL url = new URL("https://www.googleapis.com/books/v1/volumes?q=" + this.isbn);
			  HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			  InputStream inputStream = httpURLConnection.getInputStream();
			  BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			  String data = "";
			  String line = "";
			  while (line != null) {
				  line = bufferedReader.readLine();
				  data += line;
			  }
			 return new JSONObject(data);
		} catch (IOException | JSONException e) {
			e.printStackTrace();
			return null;
		}
	 }

	  private JSONObject getVolumeInfo(JSONObject JSONData) {
		  try {
			  JSONArray items = (JSONArray) JSONData.get("items");
			  JSONObject firstItem = (JSONObject) items.get(0);
			  return (JSONObject) firstItem.get("volumeInfo");
		 } catch (JSONException e) {
			  e.printStackTrace();
			  return null;
		 }
	 }

	  private String getBookTitle(JSONObject volumeInfo) {
		  try {
			  return (String) volumeInfo.get("title");
		 } catch (JSONException e) {
			  e.printStackTrace();
			  return null;
		 }
	 }

	  private String getImageURl(JSONObject volumeInfo) {
		  try {
			  JSONObject urls = (JSONObject) volumeInfo.get("imageLinks");
			  return (String) urls.get("thumbnail");
		 } catch (JSONException e) {
			  e.printStackTrace();
			  return null;
		 }
	 }

	  HashMap<String, String> getMapData() {
		  return mapData;
	 }
}
```
### How I Get ISBN Values
First implement <a href="https://github.com/natario1/CameraView">com.otaliastudios:cameraview:1.6.0</a> as your CameraView. I scan for barcodes by prompting a picture to be taken when a button is clicked. After converting this to a bitmap from a byte array I can use <a href="https://firebase.google.com/docs/ml-kit/read-barcodes">firebase-mlkit</a> to find the barcodes. The override method for taking a picture and analysing it as follows:
```java
//make sure the camera is set up before
//final CameraView camera = findViewById(R.id.camera);
//camera.setLifecycleOwner(this);

String rawValue;
camera.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(byte[] picture) {
                Bitmap bitmap = convertToBitmap(picture);
                FirebaseVisionBarcodeDetectorOptions options =
                        new FirebaseVisionBarcodeDetectorOptions.Builder()
                                .setBarcodeFormats(
                                        FirebaseVisionBarcode.FORMAT_EAN_13,
                                        FirebaseVisionBarcode.FORMAT_EAN_8)
                                .build();
                FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
                FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);
                Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
                        .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
			    	//provides a list of ISBN barcodes in the scene, here I get the first one
                            	rawValue = String.valueOf(barcodes.get(0).getRawValue());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //error occured
                            }
                        });
            }
```
Now add an OnClickListener to a button to take a picture as follows:
```java
// make sure a button is defined
//final Button scan = findViewById(R.id.scan);
scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.capturePicture();
            }
        });
```
And finally ensure that you declare a method that converts a bytearray to a bitmpa (this is used in the onPictureTaken method)
```java
private Bitmap convertToBitmap(byte[] picture) {
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(picture);
        Bitmap bitmap = BitmapFactory.decodeStream(arrayInputStream);
        return bitmap;
    }
```
