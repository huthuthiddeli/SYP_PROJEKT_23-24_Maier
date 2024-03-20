package com.mongodb.starter.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.starter.Networking.BroadcastServer;
import com.mongodb.starter.Networking.OpenTCPConnection;
import com.mongodb.starter.dtos.MbotDTO;
import com.mongodb.starter.models.Command;
import com.mongodb.starter.models.MbotEntity;
import com.mongodb.starter.services.MbotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MbotController {

    public final static Logger LOGGER = LoggerFactory.getLogger(MbotController.class);
    private final MbotService MbotService;
    private final ObjectMapper mapper = new ObjectMapper();
    private OpenTCPConnection openTCPConnection = new OpenTCPConnection();

    public MbotController(MbotService mbotService) {

        this.MbotService = mbotService;
    }

    @PostMapping("/mbot")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody MbotDTO saveToDB(@RequestBody MbotDTO CarDTO) {
        return MbotService.save(CarDTO);
    }

    @GetMapping("/mbots")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody List<MbotDTO> getAllDB() {
        return MbotService.findAll();
    }

    @PostMapping("/mbot/command")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody String postCommands(@RequestBody Command command) throws IOException {
        openTCPConnection.connectToMbot();

        return openTCPConnection.sendPackageToMbot(command);
    }

    @PostMapping("/mbot/Data")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody void postData(@RequestBody MbotDTO item){

        try{
            openTCPConnection.connectTOClient();

            MbotEntity entity = item.toMbotEntity();

            openTCPConnection.sendPackageToClient(entity);
        }catch (IllegalArgumentException e){
            LOGGER.error(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //openTCPConnection.connectToClient();


    }


    @PostMapping("/mbot/commandQueue")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody String getCommandQueue(@RequestBody Command command) throws IOException {
        if(!openTCPConnection.connectToMbot()){
           return "Error!";
        }

        if(openTCPConnection.sendPackageToMbot(command) == null){
            return "Error";
        }

        return "Worked!";
    }

    @GetMapping("/car/{id}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody MbotDTO getCar(@PathVariable String id) {
        MbotDTO CarDTO = MbotService.findOne(id);
        if (CarDTO == null) return null;
        return CarDTO;
    }

    @GetMapping("cars/{ids}")
    @ResponseStatus(HttpStatus.OK)
    public List<MbotDTO> getCars(@PathVariable String ids) {
        List<String> listIds = List.of(ids.split(","));
        return MbotService.findAll(listIds);
    }

    @GetMapping("tests")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody String items(){



        return "[{\"ID\": 1, \"IP\": \"10.10.1.1.\", \"Name\":\"Johannesbeersaft\", \"Velocity\": 66}," +
                "{\"ID\": 2, \"IP\": \"10.10.1.3.\", \"Name\":\"Davidseier\", \"Velocity\": 33}]";
    }

    @GetMapping("test")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody String item() throws JsonProcessingException {
        MbotEntity m = new MbotEntity(2.5f, new int[]{1,2,3}, 3, new int[]{1,2,3,4}, 95, 22);

        return mapper.writeValueAsString(m);
    }


    @GetMapping("mbots/count")
    @ResponseStatus(HttpStatus.OK)
    public Long getCount() {
        return MbotService.count();
    }

    @DeleteMapping("mbot/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public Long deleteCar(@PathVariable String id) {
        return MbotService.delete(id);
    }

    @DeleteMapping("mbots")

    public Long deleteCars() {
        return MbotService.deleteAll();
    }

    @PutMapping("mbot")
    @ResponseStatus(HttpStatus.CREATED)
    public MbotDTO putCar(@RequestBody MbotDTO CarDTO) {
        return MbotService.update(CarDTO);
    }

    @PutMapping("mbots")
    @ResponseStatus(HttpStatus.CREATED)
    public Long putCars(@RequestBody List<MbotDTO> carEntities) {
        return MbotService.update(carEntities);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public final Exception handleAllExceptions(RuntimeException e) {
        LOGGER.error("Internal server error.", e);
        return e;
    }
}
