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
import java.util.*;

@RestController
@RequestMapping("/api")
public class MbotController {

    public final static Logger LOGGER = LoggerFactory.getLogger(MbotController.class);
    private final MbotService MbotService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Server server = Server.getServer();


    private HashMap<String, MbotDTO> lastPackage = new HashMap<>();
    private int counter = 0;
    private Command prevCommand;


    public MbotController(MbotService mbotService) {

        this.MbotService = mbotService;
    }

    @GetMapping("/savedMbots")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody List<MbotDTO> getAllDB() {
        return MbotService.findAll();
    }

    /**
     *  <h>Get Data from MBOT. Every 25th item will be saved in the database. All of these will be directed to the Client</h>
     * @param item Sensordata object from MBOT
     * @throws JsonProcessingException
     * @throws IOException
     */
    @PostMapping("/mbot/Data")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody void postData(@RequestBody MbotDTO item) throws JsonProcessingException, IOException {

        try{
            lastPackage.put(item.toMbotEntity().getIP(), item);

            if(counter >= 25){
                LOGGER.info("[MbotController]\tData receivied!");
                MbotService.save(item);
                counter = 0;
            }

        }catch (IllegalArgumentException e){
            LOGGER.error(e.getMessage());
        }finally {
            counter++;
        }
    }


    @PostMapping("/mbot/commandQueue")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody boolean getCommandQueue(@RequestBody Command command) throws IOException {

        if(command == null){
            return false;
        }

        if(Objects.equals(prevCommand, command) && prevCommand == null){
            return false;
        }

        try{
            if(server.SendCommandToClient(command)){
                return false;
            }
        }catch (Exception ex){
            LOGGER.error("[MBOTCONTROLLER]\t" +ex.getMessage());
            LOGGER.error("[MBOTCONTROLLER]\t" + ex.getCause().getMessage());
        }

        return true;
    }

    @GetMapping("/mbot/{id}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody MbotDTO getMbot(@PathVariable String id) {
        MbotDTO CarDTO = MbotService.findOne(id);
        if (CarDTO == null) return null;
        return CarDTO;
    }

    @GetMapping("/mbots")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody String getActiveMbots() throws JsonProcessingException {

        ArrayList<MbotDTO> list = new ArrayList<>();

        if(!lastPackage.isEmpty()){
            lastPackage.forEach((v,k) -> {
                list.add(k);
            });
        }else{
            LOGGER.info("[MbotController]\tTEST DATA SENT!");
            list.add(new MbotDTO(2.5f, new ArrayList<Integer>(Arrays.asList(1,2,3,6,8,9,99)), 3,
                    new ArrayList<Integer>(Arrays.asList(1,2,3,4,6))
                    , 95, 22, "1.12.23.4"));
        }


        return mapper.writeValueAsString(list);
    }

    //TODO: MAKE IT WORK {TRANSACTIONS ARE NOT WORKING WITH MONGODB WITHOUT SETTINGS}
    @DeleteMapping("/mbots/sure")
    public Long deleteCars() {
        return MbotService.deleteAll();
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public final Exception handleAllExceptions(RuntimeException e) {
        LOGGER.error("Internal server error.", e);
        return e;
    }
}