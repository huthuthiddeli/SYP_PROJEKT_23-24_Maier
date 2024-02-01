import Route from '@ioc:Adonis/Core/Route'



Route.get('/mBot', async () => {
    return "mBot route"
})

Route.post('/mBot/', async (data : any) => {
    console.log(data);
})