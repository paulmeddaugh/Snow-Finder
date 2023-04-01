export default class ZipCodeRequest {
    zipCode: number;
    radius: number;

    constructor(zipCode: number = 0, radius: number = 0) {
        this.zipCode = zipCode;
        this.radius = radius;
    }
}