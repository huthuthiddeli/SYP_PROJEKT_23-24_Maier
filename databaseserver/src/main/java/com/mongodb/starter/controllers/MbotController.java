package com.mongodb.starter.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.starter.Networking.Server;
import com.mongodb.starter.dtos.MbotDTO;
import com.mongodb.starter.models.Command;
import com.mongodb.starter.models.MbotEntity;
import com.mongodb.starter.services.MbotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MbotController {

    public final static Logger LOGGER = LoggerFactory.getLogger(MbotController.class);
    private final MbotService MbotService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Server server = Server.getServer();

    public MbotController(MbotService mbotService) {

        this.MbotService = mbotService;
    }

    /**
     * <p> Creates an entry in the mongoDB </p>
     *
     *
     * @param mbotDTO Sensordata object
     * @return If successfull return the given Object otherwise null
     */
    @PostMapping("/mbot")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody MbotDTO saveToDB(@RequestBody MbotDTO mbotDTO) {
        return MbotService.save(mbotDTO);
    }

    @GetMapping("/mbots")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody List<MbotDTO> getAllDB() {
        return MbotService.findAll();
    }

    @PostMapping("/mbot/command")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody String postCommands(@RequestBody Command command) throws IOException {

        return "";
    }

    @PostMapping("/mbot/Data")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody void postData(@RequestBody MbotDTO item) throws JsonProcessingException, IOException {
        LOGGER.debug(mapper.writeValueAsString(item));

        try{
            MbotEntity entity = item.toMbotEntity();
        }catch (IllegalArgumentException e){
            LOGGER.error(e.getMessage());
        }
    }


    @PostMapping("/mbot/commandQueue")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody String getCommandQueue(@RequestBody Command command) throws IOException {


        try{
            if(server.sendCommandToClient(command)){
                return "Worked!";
            }
        }catch (Exception ex){
            LOGGER.error(ex.getMessage());
            LOGGER.error(ex.getCause().getMessage());
        }




        return "Error!";
    }

    @GetMapping("/mbot/{id}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody MbotDTO getMbot(@PathVariable String id) {
        MbotDTO CarDTO = MbotService.findOne(id);
        if (CarDTO == null) return null;
        return CarDTO;
    }

    @GetMapping("/mbot/{ids}")
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
        MbotEntity m = new MbotEntity(2.5f, new ArrayList<Integer>(Arrays.asList(1,2,3,6,8,9,99)), 3,
                new ArrayList<Integer>(Arrays.asList(1,2,3,4,6))
                , 95, 22);

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
