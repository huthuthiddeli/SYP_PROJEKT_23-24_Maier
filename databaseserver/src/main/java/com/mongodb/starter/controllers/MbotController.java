package com.mongodb.starter.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.starter.ConnectionType;
import com.mongodb.starter.Networking.BroadcastServer;
import com.mongodb.starter.Networking.Server;
import com.mongodb.starter.dtos.MbotDTO;
import com.mongodb.starter.models.Command;
import com.mongodb.starter.services.MbotService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

@RestController
@RequestMapping("/api")
public class MbotController {

    public final static Logger LOGGER = LoggerFactory.getLogger(MbotController.class);
    private final MbotService mbotService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Server server = Server.getServer();

    private int counter = 0;
    private boolean SUICIDE_PREVENTION = false;


    public MbotController(MbotService mbotService) {

        this.mbotService = mbotService;
    }


    @GetMapping("/savedMbots")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all entrys saved inside of the MongoDB")
    public @ResponseBody List<MbotDTO> getAllDB() {
        List<MbotDTO> entities = this.mbotService.findAll();

        return entities;
    }


    /**
     *  <h>Get Data from MBOT. Every 25th item will be saved in the database. All of these will be directed to the Client</h>
     * @param item Sensordata object from MBOT
     * @throws JsonProcessingException Because of mapping?
     * @throws IOException Socketproblems
     */
    @PostMapping("/mbot/Data")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody MbotDTO postData(@RequestBody MbotDTO item) throws JsonProcessingException, IOException {
        try{
            server.SendSensorDataToClient(item.toMbotEntity());
            if(counter >= 25){
                //LOGGER.info("[MBOTController]\t:" + item.toString());
                LOGGER.info("[MbotController]\tData receivied!");
                mbotService.save(item);
                counter = 0;
            }

        } catch (IllegalArgumentException e) {
            LOGGER.error("error: " + e.getMessage());
        } catch (Exception ignored){
        }finally {
            counter++;
        }
        return null;
    }

    @PostMapping("/mbot/DataUDP")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody void postDataUDP(@RequestBody MbotDTO item) {

        try{

            if(counter >= 25){
                LOGGER.info("[MBOTController]\t:" + item.toString());
                LOGGER.info("[MbotController]\tData receivied!");
                server.SendSensorDataToClient(item.toMbotEntity());
                mbotService.save(item);
                counter = 0;
            }

        }catch (IllegalArgumentException e){
            LOGGER.error("Error UDP: " + e.getMessage());
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            counter++;
        }
    }


    //ROUTE USED BY THE CLIENT TO SEND COMMANDS TO THE MBOT
    @PostMapping("/mbot/commandQueue")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody boolean getCommandQueueUDP(@RequestBody Command command) {

        if(command == null){
            return false;
        }

        try{
            // TCP SOCKET FOR COMMANDS IS OUTDATED!
            if(server.SendCommandToClient(command)){
                return false;
            }
        }catch (Exception ex){
            LOGGER.error("[MBOTCONTROLLER]\tCommandquqeue: " +ex.getMessage());
        }

        return true;
    }

    //GET A LIST OF ALL ACTIVE MBOTS OR A TEST MBOT
    @GetMapping("/mbots")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody String getActiveMbots() throws JsonProcessingException {

        ArrayList<MbotDTO> list = new ArrayList<>();

        if(BroadcastServer.getMbotSockets().isEmpty()){
            LOGGER.info("[MbotController]\tTEST DATA SENT!");
            list.add(
                new MbotDTO(2.5f,
                    new ArrayList<>(Arrays.asList(1,2,3)),
                    3,
                    new ArrayList<>(Arrays.asList(1,2,3,4)),
                    95, 
                    22, 
                    ConnectionType.MBOT_TEST_DATA, 
                    "1.12.23.4")
                    );
        }else{

            LOGGER.info("[MbotController]\tNORMAL DATA SEND: ");
            LOGGER.info(String.valueOf(BroadcastServer.getMbotSockets().size()));

            for(InetAddress s : BroadcastServer.getMbotSockets()){
                list.add(new MbotDTO(0f, 
                new ArrayList<>(Arrays.asList(0,0,0)), 
                0,
                new ArrayList<>(Arrays.asList(0,0,0,0)),
                0, 
                0, 
                ConnectionType.MBOT_TEST_DATA, 
                s.toString()));
            }
        }

        return mapper.writeValueAsString(list);
    }

    @GetMapping("/toggle-suicide")
    public boolean toggleSuicideMode(Command command){

        if(server.SendSuicideToggle(command, SUICIDE_PREVENTION)){
            SUICIDE_PREVENTION = !SUICIDE_PREVENTION;
        }

        return SUICIDE_PREVENTION;
    }


    @DeleteMapping("/mbots/sure")
    public void deleteCars() {
        mbotService.deleteAll();
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public final Exception handleAllExceptions(RuntimeException e) {
        LOGGER.error("Internal server error.", e);
        return e;
    }

}