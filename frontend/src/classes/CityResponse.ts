export default class CityResponse {
    hasSnow: boolean;
    name: string;
    state: string;
    snowDepth: number;
    temp: number;
    distanceAway: number;

    constructor(hasSnow: boolean = false, name: string = '', state: string = '', snowDepth: number = 0,
        temp: number = 0, distanceAway: number = 0) {

        this.hasSnow = hasSnow;
        this.name = name;
        this.state = state;
        this.snowDepth = snowDepth;
        this.temp = temp;
        this.distanceAway = distanceAway;
    }

    toString() {
        return `${this.snowDepth} °in of snow found in ${this.name}, ${this.state}`
            + `${this.distanceAway ? `, which is ${this.distanceAway} miles away` : ''}`
            + ` (${this.temp} °F)`
    }
}