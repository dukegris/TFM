import { BrowserWindow } from 'electron';
import * as path from "path";
import * as url from 'url'
import * as fs from "fs";

export default class MainWindow {
	
	static mainWindow: Electron.BrowserWindow = null;
	static application: Electron.App = null;
	static ipcMain: Electron.IpcMain = null;
	static BrowserWindow = null;

	static main(app: Electron.App, ipcMain: Electron.IpcMain, browserWindow: typeof BrowserWindow) {
		// we pass the Electron.App object and the  
		// Electron.BrowserWindow into this function 
		// so this class has no dependencies. This 
		// makes the code easier to write tests for 
		MainWindow.BrowserWindow = browserWindow;
		MainWindow.application = app;
		MainWindow.ipcMain = ipcMain;		

		MainWindow.application.on('window-all-closed', MainWindow.onWindowAllClosed);
		MainWindow.application.on('activate', MainWindow.onActivate);
		MainWindow.application.on('ready', MainWindow.onReady);

		MainWindow.ipcMain.on('getFiles', MainWindow.onGetFiles)
	}
	
	static onGetFiles(event: Electron.IpcMainEvent, ...args: any[]) {
		const files = fs.readdirSync(__dirname);
		MainWindow.mainWindow.webContents.send("getFilesResponse", files);
	}

	private static createWindow() {
		MainWindow.mainWindow = new MainWindow.BrowserWindow({
			width: 800,
			height: 600,
			webPreferences: {
				nodeIntegration: true
				//preload: path.join(__dirname, "preload.js"),
			}
		});
		MainWindow.mainWindow.loadURL(
			url.format({
			  pathname: path.join(__dirname, './TFMDesktop/index.html'),
			  protocol: 'file:',
			  slashes: true,
			}));
		MainWindow.mainWindow.on('closed', MainWindow.onClose);
	}

	private static onWindowAllClosed() {
		if (process.platform !== 'darwin') {
			MainWindow.application.quit();
		}
	}

	private static onClose() {
		// Dereference the window object. 
		MainWindow.mainWindow = null;
	}

	private static onActivate() {
		if (MainWindow.mainWindow === null) {
			MainWindow.createWindow();
		}
	}
	
	private static onReady() {
		if (MainWindow.mainWindow == null) {
			MainWindow.createWindow();
		}
	}

}