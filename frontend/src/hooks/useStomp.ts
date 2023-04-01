import { useEffect, useState } from "react";
import { Client } from "@stomp/stompjs";

// let stompClient: CompatClient;
let stompClient: Client;

export default function useStomp(connectionUrl: string) {
    const [message, setMessage] = useState('');

    useEffect(() => {
        stompClient = new Client({ 
            brokerURL: connectionUrl,
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });
        stompClient.onConnect = () => {
            console.log("Connected!!");
            stompClient.subscribe('/results/snow', function (msg) {
                if (!msg.body) return;
                setMessage(String(msg.body));
            });
        };
        stompClient.onStompError = function (frame) {
            // Will be invoked in case of error encountered at Broker
            // Bad login/passcode typically will cause an error
            // Complaint brokers will set `message` header with a brief message. Body may contain details.
            // Compliant brokers will terminate the connection after any error
            console.log('Broker reported error: ' + frame.headers['message']);
            console.log('Additional details: ' + frame.body);
        };
        stompClient.activate();
    }, [connectionUrl]);

    const send = (endpoint: string, body?: any) => {
        stompClient.publish({ 
            destination: endpoint, 
            body,
            skipContentLengthHeader: true
        });
    };

    return {
        message,
        send
    }
}