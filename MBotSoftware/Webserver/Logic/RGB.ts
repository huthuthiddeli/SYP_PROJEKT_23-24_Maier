class RGB{
    private R : number;
    private G : number;
    private B : number;

    constructor(R : number, G : number, B : number){
        this.R = R;
        this.G = G;
        this.B = B;
    }

    public getR() : number {
        return this.R;
    }

    public getG() : number {
        return this.G;
    }

    public getB() : number {
        return this.B;
    }
}


export default RGB;