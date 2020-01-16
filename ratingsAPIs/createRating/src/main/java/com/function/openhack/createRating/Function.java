package com.function.openhack.createRating;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.FloatSerializer;
import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    /**
     * This function listens at endpoint "/api/HttpTrigger-Java". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpTrigger-Java&code={your function key}
     * 2. curl "{your host}/api/HttpTrigger-Java?name=HTTP%20Query&code={your function key}"
     * Function Key is not needed when running locally, it is used to invoke function deployed to Azure.
     * More details: https://aka.ms/functions_authorization_keys
     */
    ObjectMapper mapper = new ObjectMapper();
    //HttpHost target = new HttpHost("weather.yahooapis.com", 80, "http");
    HttpHost target = new HttpHost("serverlessohuser.trafficmanager.net", 80, "http");

    Boolean userIdIsValid(String userId) {
        // {"userId":"cc20a6fb-a91f-4192-874d-132493685376",
        // "userName":"doreen.riddle","fullName":"Doreen Riddle"}
        DefaultHttpClient httpclient = new DefaultHttpClient();
        final String API = "/api/GetUser?userId="+userId;
        HttpGet getRequest = new HttpGet(API);
        Boolean returnValue;
        try {
            HttpResponse response = httpclient.execute(target, getRequest);
            if(response.getStatusLine().getStatusCode() != 200) {
                returnValue = false;
            }
            else returnValue = true;
        } catch (IOException ex) {
            ex.printStackTrace();
            returnValue = false;
        }
        httpclient.close();
        return returnValue;
    }

    Boolean productIdIsValid(String productId) {
        // {"productId":"75542e38-563f-436f-adeb-f426f1dabb5c","productName":"Starfruit Explosion","productDescription":"This starfruit ice cream is out of this world!"}
        DefaultHttpClient httpclient = new DefaultHttpClient();
        final String API = "/api/GetProduct?productId="+productId;
        HttpGet getRequest = new HttpGet(API);
        Boolean returnValue;
        try {
            HttpResponse response = httpclient.execute(target, getRequest);
            if(response.getStatusLine().getStatusCode() != 200) {
                returnValue = false;
            }
            else returnValue = true;
        } catch (IOException ex) {
            ex.printStackTrace();
            returnValue = false;
        }
        httpclient.close();
        return returnValue;
    }

    @FunctionName("create-rating")
    public HttpResponseMessage run(
        @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<String>> request,
        @CosmosDBOutput(name = "cosmosOutputBinding", databaseName = "Products", collectionName = "Ratings", connectionStringSetting = "CosmosDBConnectionString") OutputBinding<POSTinput> document,
        final ExecutionContext context) 
        {
            if (request.getBody().isPresent()) {
                try {
                    POSTinput body = mapper.readValue(request.getBody().get(), POSTinput.class);
                    Boolean requestIsValid = true;

                    // Validate request
                    Integer rating = body.rating;
                    if (rating < 0 | rating > 5) requestIsValid = false; 
                    if(!userIdIsValid(body.userId)) requestIsValid = false; 
                    if(!productIdIsValid(body.productId)) requestIsValid = false;
                    
                    // Prepare an set review object
                    body.id = UUID.randomUUID().toString();
                    body.timeStamp = new Timestamp(new Date().getTime()).toString();
                    document.setValue(body);

                    // Send response
                    return request
                        .createResponseBuilder(HttpStatus.ACCEPTED)
                        .body(body)
                        .build();
                    } catch(Exception e) {
                        e.printStackTrace();
                    return request
                        .createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("There was a problem processing the request")
                        .build();
                    }
            } else {
                return request
                    .createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Request body could not be cast to JSON")
                    .build();
            }
    }
}
