/**
 *  Z-Wave Siren Switch Generic
 *  Build 2017082901
 *
 *  Adapted from Z-Wave Switch Generic
 *
 *  Copyright 2017 Jordan Markwell
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  ChangeLog:
 *      
 *      Earlier:
 *          Creation
 *      
 *      2017082901:
 *          Added code for non-GE model compatibility back in
 *          Updated based on updated Z-Wave Switch Generic to include CRC16 encapsulation
 *      
 */
metadata {
    definition(name: "Z-Wave Siren Switch Generic", namespace: "jmarkwell", author: "Jordan Markwell", ocfDeviceType: "oic.d.switch") {
        capability "Actuator"
        capability "Alarm"
        capability "Health Check"
        capability "Polling"
        capability "Refresh"
        capability "Sensor"
        capability "Switch"
        
        fingerprint inClusters: "0x25", deviceJoinName: "Z-Wave Switch"
        fingerprint mfr:"001D", prod:"1A02", model:"0334", deviceJoinName: "Leviton Appliance Module"
        fingerprint mfr:"0063", prod:"4F50", model:"3031", deviceJoinName: "GE Plug-in Outdoor Switch"
        fingerprint mfr:"001D", prod:"1D04", model:"0334", deviceJoinName: "Leviton Outlet"
        fingerprint mfr:"001D", prod:"1C02", model:"0334", deviceJoinName: "Leviton Switch"
        fingerprint mfr:"001D", prod:"0301", model:"0334", deviceJoinName: "Leviton 15A Switch"
        fingerprint mfr:"001D", prod:"0F01", model:"0334", deviceJoinName: "Leviton 5A Incandescent Switch"
        fingerprint mfr:"001D", prod:"1603", model:"0334", deviceJoinName: "Leviton 15A Split Duplex Receptacle"
        fingerprint mfr:"011A", prod:"0101", model:"0102", deviceJoinName: "Enerwave On/Off Switch"
        fingerprint mfr:"011A", prod:"0101", model:"0603", deviceJoinName: "Enerwave Duplex Receptacle"
    }
    
    simulator {
        status "both": "command: 2003, payload: FF"
        status "off": "command: 2003, payload: 00"
        
        reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
        reply "200100,delay 100,2502": "command: 2503, payload: 00"
    }
    
    tiles(scale: 2) {
        multiAttributeTile(name: "alarm", type: "generic", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.alarm", key: "PRIMARY_CONTROL") {
                attributeState "both", label: 'alarm!', action: "alarm.off", icon: "st.alarm.alarm.alarm", backgroundColor: "#e86d13"
                attributeState "off", label: 'off', action: "alarm.siren", icon: "st.alarm.alarm.alarm", backgroundColor: "#ffffff"
            }
        }
        
        standardTile("refresh", "device.alarm", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
        }
        
        main "alarm"
        details(["alarm", "refresh"])
    }
}

def installed(){
    // device-watch simply pings if no device events received for checkInterval duration of 32min = 2 * 15min + 2min lag time
    sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def updated(){
    // device-watch simply pings if no device events received for checkInterval duration of 32min = 2 * 15min + 2min lag time
    sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def getCommandClassVersions() {
    [
        0x20: 1,  // basic
        0x56: 1,  // crc16Encap
        0x70: 1,  // configuration
    ]
}

def parse(String description) {
    def result = null
    def cmd = zwave.parse(description, commandClassVersions)
    
    if (cmd) {
        result = createEvent(zwaveEvent(cmd))
    }
    
    if (result?.name == 'hail' && hubFirmwareLessThan("000.011.00602")) {
        result = [result, response(zwave.basicV1.basicGet())]
        log.debug "hailed: requesting state update"
    } else {
        log.debug "parse returned: ${result?.descriptionText}"
    }
    
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    [name: "alarm", value: cmd.value ? "both" : "off", type: "physical"]
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
    [name: "alarm", value: cmd.value ? "both" : "off", type: "physical"]
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    [name: "alarm", value: cmd.value ? "both" : "off", type: "digital"]
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    def value = "when off"
    if (cmd.configurationValue[0] == 1) {value = "when on"}
    if (cmd.configurationValue[0] == 2) {value = "never"}
    [name: "indicatorStatus", value: value, display: false]
}

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
    [name: "hail", value: "hail", descriptionText: "alarm button was pressed", displayed: false]
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    log.debug "manufacturerId:   ${cmd.manufacturerId}"
    log.debug "manufacturerName: ${cmd.manufacturerName}"
    log.debug "productId:        ${cmd.productId}"
    log.debug "productTypeId:    ${cmd.productTypeId}"
    
    def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
    updateDataValue("MSR", msr)
    updateDataValue("manufacturer", cmd.manufacturerName)
    
    createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
    def versions = commandClassVersions
    def version = versions[cmd.commandClass as Integer]
    def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
    def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
    if (encapsulatedCommand) {
        zwaveEvent(encapsulatedCommand)
    }
}

// unknown commands
def zwaveEvent(physicalgraph.zwave.Command cmd) {
    [:]
}

def on() {
    delayBetween([
        zwave.basicV1.basicSet(value: 0xFF).format(),
        zwave.switchBinaryV1.switchBinaryGet().format()
    ])
}

def both() {
    delayBetween([
        zwave.basicV1.basicSet(value: 0xFF).format(),
        zwave.switchBinaryV1.switchBinaryGet().format()
    ])
}

def siren() {
    delayBetween([
        zwave.basicV1.basicSet(value: 0xFF).format(),
        zwave.switchBinaryV1.switchBinaryGet().format()
    ])
}

def strobe() {
    delayBetween([
        zwave.basicV1.basicSet(value: 0xFF).format(),
        zwave.switchBinaryV1.switchBinaryGet().format()
    ])
}

def off() {
    delayBetween([
        zwave.basicV1.basicSet(value: 0x00).format(),
        zwave.switchBinaryV1.switchBinaryGet().format()
    ])
}

// used by device-watch
def ping() {
    delayBetween([
        zwave.switchBinaryV1.switchBinaryGet().format(),
        zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
    ])
}

def poll() {
    delayBetween([
        zwave.switchBinaryV1.switchBinaryGet().format(),
        zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
    ])
}

def refresh() {
    delayBetween([
        zwave.switchBinaryV1.switchBinaryGet().format(),
        zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
    ])
}

def invertSwitch(invert=true) {
    if (invert) {
        zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 4, size: 1).format()
    }
    else {
        zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 4, size: 1).format()
    }
}
