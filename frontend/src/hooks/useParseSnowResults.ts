import { useEffect, useState } from "react";
import CityResponse from "../classes/CityResponse";

export default function useLoadSnowResults (props: { message: string }) {

    const { message } = props;

    const [citiesWithSnow, setCitiesWithSnow] = useState(0);
    const [loadedNumber, setLoadedNumber] = useState(0);
    const [toLoadNumber, setToLoadNumber] = useState(0);
    const [log, setLog] = useState('');
    const [city, setCity] = useState(new CityResponse());

    useEffect(() => {

        console.log(message);

        if (message[0] === 'X' || message[0] === 'O') {
            const m = message.split(',');
            const cityProps = new CityResponse(
                m[0] === 'X', m[1], m[2], parseInt(m[3]), parseInt(m[4]), parseInt(m[5])
            );
            setCity(cityProps);

            if (cityProps.hasSnow) {
                setCitiesWithSnow(prev => ++prev);
                setLog(prev => prev + '<br />' + cityProps.toString());
            }
            setLoadedNumber(prev => ++prev);
        } else if (message.indexOf(' results') !== -1) {
            setToLoadNumber(Number(message.substring(0, message.indexOf(' '))));
        } else {
            setLog(prev => prev + '<br />' + message);
        }
    }, [message]);

    const reset = () => {
        setCitiesWithSnow(0);
        setLoadedNumber(0);
        setToLoadNumber(0);
        setLog('');
    }

    return {
        citiesWithSnow,
        loadedNumber,
        toLoadNumber,
        log,
        city,
        reset
    }
}