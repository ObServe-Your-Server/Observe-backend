package com.observe.os1.v1.metrics;

import com.observe.os1.v1.PrometheusRestClient;
import com.observe.os1.v1.prometheusQueries.DiskQueries;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/v1/metrics/disk")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DiskResource {

    @Inject
    @RestClient
    PrometheusRestClient prometheusRestClient;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/free-space-in-gb")
    @Operation(summary = "Get free disk space in gb")
    @APIResponse(
            responseCode = "200",
            description = "Prometheus query response for free disk space in GB",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(
                            description = "Response containing free disk space in GB",
                            example = """
                                    {
                                      "status": "success",
                                      "data": {
                                        "resultType": "matrix",
                                        "result": [
                                          {
                                            "metric": {
                                              "fstype": "ext4",
                                              "instance": "host.docker.internal:9100"
                                            },
                                            "values": [
                                              [1752966880, "100.5"],
                                              [1752966882, "99.8"]
                                            ]
                                          }
                                        ]
                                      }
                                    }
                                    """
                    )
            )
    )
    public Response getFreeSpaceInGB(
            @QueryParam("startTime")
            @Parameter(
                    description = "Start time as Unix timestamp",
                    example = "1752966880"
            ) Long startTime,

            @QueryParam("endTime")
            @Parameter(
                    description = "End time as Unix timestamp",
                    example = "1752966940"
            ) Long endTime,

            @QueryParam("interval")
            @Parameter(
                    description = "Interval in seconds between data points",
                    example = "15"
            ) Long interval
    ) {
        // check the parameters
        if (startTime == null || endTime == null || interval == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Missing required query parameters: startTime, endTime, interval")
                    .build();
        }
        if (startTime < 0 || endTime < 0 || interval <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid query parameters: startTime, endTime must be non-negative and interval must be positive")
                    .build();
        }

        return prometheusRestClient.universalTimeQuery(
                DiskQueries.DISK_USAGE_IN_GB.toString(),
                startTime.toString(),
                endTime.toString(),
                interval + "s"
        );
    }

}
