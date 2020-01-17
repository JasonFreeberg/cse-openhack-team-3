
using System.IO;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Azure.WebJobs;
using Microsoft.Azure.WebJobs.Extensions.Http;
using Microsoft.AspNetCore.Http;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json;
using System.Collections.Generic;
using System.Net.Http;
using System.Net;
using System.Text;

namespace FunctionApp2
{
    public static class Function1
    {

        [FunctionName("Ratings")]
        public static async Task<HttpResponseMessage> GetRatings(
          [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "GetRatings")]
                HttpRequest req,
          [CosmosDB(
                databaseName: "Products",
                collectionName: "Ratings",
                ConnectionStringSetting = "CosmosDBConnection",
                SqlQuery = "SELECT * FROM c")]
                IEnumerable<Product> products,
          ILogger log)
        {
            // logs information from the json data
            log.LogInformation("C# HTTP trigger function processed a request.");
            foreach (Product product in products)
            {
                log.LogInformation(product.ProductId);
                log.LogInformation(product.UserId);

            }

            // serializes the json data so it can be read when we hit the end point
            var jsonToReturn = JsonConvert.SerializeObject(products);

            return new HttpResponseMessage(HttpStatusCode.OK)
            {
                Content = new StringContent(jsonToReturn, Encoding.UTF8, "application/json")
            };
        }


        [FunctionName("Rating")]
        public static async Task<HttpResponseMessage> GetRating(
            [HttpTrigger(AuthorizationLevel.Anonymous, "get", "post", Route = "GetRating/{id}")] HttpRequest req, [CosmosDB(
            databaseName: "Products",
            collectionName: "Ratings",
            ConnectionStringSetting = "CosmosDBConnection",
            SqlQuery = "SELECT * FROM Products c WHERE c.id = {id}")] IEnumerable<Product> product, 
            ILogger log)
        {

            // logs information from the json data
            log.LogInformation("C# HTTP trigger function processed a request.");

            foreach (Product i in product)
            {
                log.LogInformation(i.ProductId);
                log.LogInformation(i.UserId);
            }

            // serializes the json data so it can be read when we hit the end point
            var jsonToReturn = JsonConvert.SerializeObject(product);

            return new HttpResponseMessage(HttpStatusCode.OK)
            {
                Content = new StringContent(jsonToReturn, Encoding.UTF8, "application/json")
            };
        }
    }
}
