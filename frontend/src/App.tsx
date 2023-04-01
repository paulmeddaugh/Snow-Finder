import React, { createRef, useEffect, useRef, useState } from 'react';
import logo from './logo.svg';
import './App.css';
import useStomp from './hooks/useStomp';
import useLoadSnowResults from './hooks/useParseSnowResults';
import ZipCodeRequest from './classes/ZipCodeRequest';

const SOCKET_URL = 'ws://localhost:8080/snow-finder';

const snowballFight = require('./resources/snowball-fight-cropped.gif');
const SNOWBALL_FIGHT_RESTART_SECS = 15;

function App() {

    const { message, send } = useStomp(SOCKET_URL);
    const { citiesWithSnow, toLoadNumber, loadedNumber, city, log, reset } = useLoadSnowResults({ message });

    const [zipCode, setZipCode] = useState(0);
    const [radius, setRadius] = useState(0);
    const [searchType, setSearchType] = useState('zipCode'); // 'id' prop of radio button checked

    const snowballFightRef = useRef<HTMLImageElement>(null);

    const onTabChange = (e: any) => {
        setSearchType(String(e.target.id));
    }

    const submit = () => {
        reset();
        if (searchType === 'zipCode') {
            send('/find/snowUsingZip', JSON.stringify(new ZipCodeRequest(zipCode, radius)))
        } else {
            send("/find/snowInUS");
        }
    }

    useEffect(() => {
        setTimeout(restartSnowballFight, Math.random() * SNOWBALL_FIGHT_RESTART_SECS * 1000);
    }, [snowballFightRef.current?.getAttribute('src')]);

    const restartSnowballFight = () => {
        if (!snowballFightRef.current) return;

        let s = snowballFightRef.current.getAttribute('src');
        snowballFightRef.current.setAttribute('src', '');
        snowballFightRef.current.setAttribute('src', s ?? '');
    }

    return (
        <div className={'body'}>
            <div className='blueSky'></div>
            <div className='inputContainer'>
                <h1>SnowFinder</h1>
                <div className='tabs d-inline'>
                    <input 
                        className='tab invisible' 
                        type="radio" 
                        id="zipCode" 
                        name="findType" 
                        value="snowUsingZip" 
                        onChange={onTabChange}
                        checked={searchType === 'zipCode'}
                    />
                    <label 
                        className={`tabLabel ${searchType === 'zipCode' ? 'activeTab' : ''}`} 
                        htmlFor="zipCode"
                    >
                        Zip Code Radius
                    </label>
                    <span>|</span>
                    <input 
                        className='tab invisible' 
                        type="radio" 
                        id="USCities" 
                        name="findType" 
                        value="snowInUS" 
                        onChange={onTabChange}
                        checked={searchType === 'USCities'}
                    />
                    <label 
                        className={`tabLabel ${searchType === 'USCities' ? 'activeTab' : ''}`}  
                        htmlFor="USCities"
                    >
                        Major US Cities
                    </label>
                </div>
                {searchType === 'zipCode' ? (
                    <div className='d-inline'>
                        <label>
                            Search Location:
                        </label>
                        <input 
                            className='input'
                            type={'text'} 
                            placeholder={'Zip Code'}
                            value={zipCode}
                            onChange={(e) => setZipCode(Number(e.target.value))}
                        />
                        <input 
                            className='input'
                            type={'text'} 
                            placeholder={'Radius'}
                            value={radius}
                            onChange={(e) => setRadius(Number(e.target.value))}
                        />
                    </div>
                ) : null}
                
                <button className={searchType === 'USCities' ? 'button-majorCities' : ''} onClick={submit}>
                    {searchType === 'USCities' ? 'Search major cities in the US' : 'Search'}
                </button>
                {toLoadNumber ? (
                    <div className='results'>
                        <div className='citiesWithSnow'>{citiesWithSnow}</div>
                        <span>&nbsp;cities found with snow (</span>
                        <div className='loaded'>{loadedNumber}</div>
                        <span>&nbsp;cities searched)</span>

                        <div dangerouslySetInnerHTML={{__html: log}} ></div>
                    </div>
                ) : null}
            </div>
            <img ref={snowballFightRef} src={snowballFight} className='snowball-fight' />
        </div>
    )
}

export default App;