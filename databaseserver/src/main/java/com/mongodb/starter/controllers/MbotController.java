package com.mongodb.starter.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.starter.ConnectionType;
import com.mongodb.starter.Networking.BroadcastServer;
import com.mongodb.starter.Networking.Server;
import com.mongodb.starter.Networking.UDP_Server;
import com.mongodb.starter.dtos.ClientDTO;
import com.mongodb.starter.dtos.MbotDTO;
import com.mongodb.starter.models.Command;
import com.mongodb.starter.services.ActualMbotService;
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
    private final ActualMbotService mbotService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Server server = Server.getServer();
    private final UDP_Server udp_server = UDP_Server.GetInstance();


    private HashMap<String, MbotDTO> lastPackage = new HashMap<>();
    private int counter = 0;
    private Command prevCommand;


    public MbotController(ActualMbotService mbotService) {

        this.mbotService = mbotService;
    }

    @GetMapping("/savedMbots")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all entrys saved inside of the MongoDB")
    public @ResponseBody List<MbotDTO> getAllDB() {
        return mbotService.findAll();
    }

    /**
     *  <h>Get Data from MBOT. Every 25th item will be saved in the database. All of these will be directed to the Client</h>
     * @param item Sensordata object from MBOT
     * @throws JsonProcessingException Because of mapping?
     * @throws IOException Socketproblems
     */
    @PostMapping("/mbot/Data")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody MbotDTO postData(@RequestBody MbotDTO item) throws IOException {
        try{
            lastPackage.put(item.toMbotEntity().getIP(), item);
            server.SendSensorDataToClient(item.toMbotEntity());
            mbotService.save(item);
            if(counter >= 25){
                item = new MbotDTO(item.ultrasonic(), item.angles(), item.sound(), item.front_light_sensors(), item.shake(), item.light(), ConnectionType.MBOT_DB_PACKAGE, item.ip());
                //LOGGER.info("[MBOTController]\t:" + item.toString());
                LOGGER.info("[MbotController]\tData receivied!");

                counter = 0;
            }

        }catch (IllegalArgumentException e){
            LOGGER.error("error: " + e.getMessage());
        }finally {
            counter++;
        }
        return null;
    }


    @PostMapping("/mbot/DataUDP")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody void postDataUDP(@RequestBody MbotDTO item) throws JsonProcessingException, IOException {

        try{
            lastPackage.put(item.toMbotEntity().getIP(), item);


            if(counter >= 25){
                LOGGER.info("[MBOTController]\t:" + item.toString());
                LOGGER.info("[MbotController]\tData receivied!");
                server.SendSensorDataToClient(item.toMbotEntity());
                mbotService.save(item);
                counter = 0;
            }

        }catch (IllegalArgumentException e){
            LOGGER.error("Error UDP: " + e.getMessage());
        }finally {
            counter++;
        }
    }


    @PostMapping("/mbot/commandQueue")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody boolean getCommandQueueUDP(@RequestBody Command command) throws IOException {

        if(command == null){
            return false;
        }

        try{
            // TCP SOCKET FOR COMMANDS IS OUTDATED!
            if(server.SendCommandToClient(command)){
                return false;
            }

            if(!udp_server.SendCommand(command)){
                return false;
            }
        }catch (Exception ex){
            LOGGER.error("[MBOTCONTROLLER]\t Commandquqeue: " +ex.getMessage());
        }

        return true;
    }
    @GetMapping("/mbots")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody String getActiveMbots() throws JsonProcessingException {

        ArrayList<ClientDTO> list = new ArrayList<>();
        LOGGER.info("[MbotController]\tNo Mbots registered in broadcast!");
        if(BroadcastServer.getMbotSockets().isEmpty()){
            LOGGER.info("[MbotController]\tTEST DATA SENT!");
            list.add(new ClientDTO(2.5f, new ArrayList<>(Arrays.asList(1,2,3,6,8,9,99)), 3,
                    new ArrayList<>(Arrays.asList(1,2,3,4,6))
                    , 95, 22, ConnectionType.MBOT_TEST_DATA, "1.12.23.4"));
        }else{

            LOGGER.info("[MbotController]\tNORMAL DATA SEND: ");
            LOGGER.info(String.valueOf(BroadcastServer.getMbotSockets().size()));

            for(InetAddress s : BroadcastServer.getMbotSockets()){
                list.add(new ClientDTO(0f, new ArrayList<>(Arrays.asList(0,0,0,0,0,0,0)), 0,
                        new ArrayList<>(Arrays.asList(0,0,0,0,0))
                        , 0, 0, ConnectionType.CONNECTION_ALIVE, s.toString()));
            }
        }


        return mapper.writeValueAsString(list);
    }

    //TODO: MAKE IT WORK {TRANSACTIONS ARE NOT WORKING WITH MONGODB WITHOUT SETTINGS}
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