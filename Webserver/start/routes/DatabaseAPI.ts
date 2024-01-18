import * as dotenv from 'dotenv'
import { ObjectId } from "mongodb";
import Route from '@ioc:Adonis/Core/Route'


Route.get('/database', async => {
    dotenv.config({path: '../.dotenv'})  
        
        
    return process.env.PORT;
});