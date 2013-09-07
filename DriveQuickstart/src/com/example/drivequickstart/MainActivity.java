package com.example.drivequickstart;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.internal.IPolylineDelegate;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;

public class MainActivity extends Activity {
  static final int REQUEST_ACCOUNT_PICKER = 1;
  static final int REQUEST_AUTHORIZATION = 2;
  static final int CAPTURE_IMAGE = 3;
  static final int DOWNLOAD_FILE = 4;  

  private static Uri fileUri;
  private static Drive service;
  private GoogleAccountCredential credential;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    credential = GoogleAccountCredential.usingOAuth2(this,Arrays.asList(DriveScopes.DRIVE_FILE));
    startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
  }

  public void upload(View v)
  {
	  Thread t = new Thread(new Runnable() {
	      @Override
	      public void run() {
	        try {
	          // File's binary content
	          java.io.File fileContent = new java.io.File("/sdcard/ex.txt");
	          FileContent mediaContent = new FileContent("text/plain", fileContent);
	          System.out.println("------------------------------------");
	          /*File bodyf = new File();
	          bodyf.setTitle("divideandprotect");
	          bodyf.setMimeType("application/vnd.google-apps.folder");
	          File filef = service.files().insert(bodyf).execute();*/
	          File body = new File();
	          body.setTitle(fileContent.getName());
	          body.setMimeType("text/plain");
	          
	          java.util.List<File> files = service.files().list().setQ("mimeType = 'application/vnd.google-apps.folder'").execute().getItems();
	          for (File f : files) {
	              System.out.println(f.getTitle() + ", " + f.getMimeType());
	              if(f.getTitle().equals("divideandprotect"))
	              body.setParents(Arrays.asList(new ParentReference().setId(f.getId())));
	          }
	          // File's metadata.
	          
	          File file = service.files().insert(body, mediaContent).execute();
	          if (file != null) {
	            showToast("Photo uploaded: " + file.getTitle());
	           // startCameraIntent();
	          }
	          
	          
	        } catch (UserRecoverableAuthIOException e) {
	          startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
	        } catch (IOException e) {
	          e.printStackTrace();
	        }
	      }
	    });
	    t.start();
	  
  }
  
  public void download(View v)
  {
	  Thread t = new Thread(new Runnable() {
	      @Override
	      public void run() {
	    	  
	          java.util.List<File> files;
			try {
				byte [] b=new byte[2048];
				files = service.files().list().setQ("mimeType = 'text/plain'").execute().getItems();
				File file=null;
		          for (File f : files) {
		              System.out.println(f.getTitle() + ", " + f.getMimeType());
		              if(f.getTitle().equals("ex.txt"))
		              {
		            	  file = service.files().get(f.getId()).execute();
		  		        InputStream ip = downloadFile(service, file);
		  		        System.out.println(ip.available()+""+file.getDownloadUrl());
				        while(ip.read(b)!=0) {
				        	
				        }
		            	  
		              }
		          }
		    	 
		        String s = new String(b);
		        System.out.println(s);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	          	      }
	      InputStream downloadFile(Drive service, File file) {
	    	  
	    	    if (file.getDownloadUrl() != null && file.getDownloadUrl().length() > 0) {
	    	      try {
	    	        HttpResponse resp =
	    	            service.getRequestFactory().buildGetRequest(new GenericUrl(file.getDownloadUrl()))
	    	                .execute();
	    	        System.out.println(resp.getContentType()+" "+resp.parseAsString()  + " "+resp.getContent());
	    	        return null;
	    	      } catch (IOException e) {
	    	        // An error occurred.
	    	        e.printStackTrace();
	    	        return null;
	    	      }
	    	    } else {
	    	      // The file doesn't have any content stored on Drive.
	    	      return null;
	    	    }
	    	  }
	      
	    }); 
	  t.start();
  }
  
  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    switch (requestCode) {
    case REQUEST_ACCOUNT_PICKER:
      if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
        String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        if (accountName != null) {
          credential.setSelectedAccountName(accountName);
          service = getDriveService(credential);
          setContentView(R.layout.activity_main);
          //startCameraIntent();
        }
      }
      break;
    case REQUEST_AUTHORIZATION:
      if (resultCode == Activity.RESULT_OK) {
      //  saveFileToDrive();
      } else {
        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
      }
      break;
    case CAPTURE_IMAGE:
      if (resultCode == Activity.RESULT_OK) {
     //   saveFileToDrive();            
      }
      break;
    }
  }

  private void startCameraIntent() {
    String mediaStorageDir = Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_PICTURES).getPath();
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
    fileUri = Uri.fromFile(new java.io.File(mediaStorageDir + java.io.File.separator + "IMG_"
        + timeStamp + ".jpg"));

    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
    startActivityForResult(cameraIntent, CAPTURE_IMAGE);
  }

/*  private void saveFileToDrive() {
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          // File's binary content
          java.io.File fileContent = new java.io.File("/sdcard/ex.txt");
          FileContent mediaContent = new FileContent("text/plain", fileContent);
          System.out.println("------------------------------------");
          /*File bodyf = new File();
          bodyf.setTitle("divideandprotect");
          bodyf.setMimeType("application/vnd.google-apps.folder");
          File filef = service.files().insert(bodyf).execute();*/
  /*        File body = new File();
          body.setTitle(fileContent.getName());
          body.setMimeType("text/plain");
          
          java.util.List<File> files = service.files().list().setQ("mimeType = 'application/vnd.google-apps.folder'").execute().getItems();
          for (File f : files) {
              System.out.println(f.getTitle() + ", " + f.getMimeType());
              if(f.getTitle().equals("divideandprotect"))
              body.setParents(Arrays.asList(new ParentReference().setId(f.getId())));
          }
          // File's metadata.
          
          File file = service.files().insert(body, mediaContent).execute();
          if (file != null) {
            showToast("Photo uploaded: " + file.getTitle());
           // startCameraIntent();
          }
          
          
        } catch (UserRecoverableAuthIOException e) {
          startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
    t.start();
  }
*/
  private Drive getDriveService(GoogleAccountCredential credential) {
    return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
        .build();
  }


  
  public void showToast(final String toast) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
      }
    });
  }
}