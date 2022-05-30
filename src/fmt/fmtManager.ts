import fs from 'react-native-fs'
import { Fmt } from './fmt';
import { FmtPanelRefType, FmtRefType, PanelDirectionType } from './fmtEnums';
import { FmtPanel } from './fmtPanel';

export type FmtModelType = {
    fmt: Fmt,
    panels: FmtPanel[]
}

export class FmtManager {
    public constructor() {
        
    }
    getMatches(regExp:RegExp, str: string): Array<string> {
        let arr = [...str.matchAll(regExp)];
        return arr.map(x => x[1]);
      }
    
    parser = (fmt:string) : FmtModelType => {
        const arr = fmt.split("\n")
        const fmtModel = new Fmt(fmt)
        const panelArr = new Array<FmtPanel>()
    
        const reg = new RegExp("/(.+?)/", "g")
        arr.forEach(line => {
            const rowStr = this.getMatches(reg, line)
            if (line.includes("/DIMENSIONS")) {
                const dimension = rowStr[0]?.split("=")[1]
                const order = rowStr[1].split("=")[1]
                fmtModel.orderCorner = order.split("").map(x => parseInt(x))
                fmtModel.Dimensions = dimension.split(",").map(x => parseInt(x))
                const splitter = line.split("=")
                fmtModel.Rows = parseInt(splitter[0])
                fmtModel.Columns = parseInt(splitter[1])
                fmtModel.refType = <FmtRefType> splitter[3]
            }
            else {
                const splitter = line.split("=")
                const newFmtPanel = new FmtPanel()
                newFmtPanel.panelName = rowStr[0]
                newFmtPanel.format = splitter[6]
                newFmtPanel.map = [...splitter].slice(0, 4).map(x => parseInt(x))
                newFmtPanel.panelRefType = <FmtPanelRefType> splitter[4]
                newFmtPanel.direction = <PanelDirectionType> splitter[5]
                panelArr.push(newFmtPanel)
            }
        })

        return {
            fmt: fmtModel,
            panels: panelArr
        }
    }
}