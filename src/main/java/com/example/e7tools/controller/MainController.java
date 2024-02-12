package com.example.e7tools.controller;

import com.example.e7tools.service.MainService;
import com.example.e7tools.constant.PcapConstant;
import com.example.e7tools.constant.UITextConstant;
import com.example.e7tools.tool.PcapTool;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import javafx.stage.Stage;
import org.pcap4j.core.PcapAddress;
import org.pcap4j.core.PcapNetworkInterface;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;


/**
 * 主界面
 * todo:仅实现功能，代码还需要精简优化
 */
public class MainController {

    @FXML
    private MenuItem menuAbout;
    @FXML
    private Label statusBar;
    @FXML
    private Button startCaptureBtn;
    @FXML
    private Button driveRefreshBtn;
    @FXML
    private Label label;
    @FXML
    public Button exportFileBtn;
    /**
     * 网卡驱动下拉框
     */
    @FXML
    private ChoiceBox<String> networkDrive;
    private final CountDownLatch latch = new CountDownLatch(1);
    PcapTool pcapTool;
    private final Map<Integer, PcapNetworkInterface> driveMap = new HashMap<>();
    MainService mainService;
    private HostServices hostServices;


    public MainController() {
        pcapTool = new PcapTool();
        mainService = new MainService();
    }

    @FXML
    protected void onAboutMenuBtn() throws IOException {
        //对话框
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(UITextConstant.ABOUT);
        alert.setHeaderText(null);
        //对话框内容
        Label about = new Label();
        Map<String, String> latestVersionMap = mainService.getVersion();
        Map<String, String> currentVersionMap = mainService.getCurrentVersion();
        String text = MessageFormat.format(
                "name: {0}\n" +
                        "current_version: {1}\n" +
                        "latest_version: {2}\n" +
                        "author: {3}\n", latestVersionMap.get("name"), currentVersionMap.get("version"), latestVersionMap.get("version"), "绿色恐龙");
        about.setText(text);
        //超链接
        Hyperlink hyperlink = new Hyperlink("Gitee地址");
        hyperlink.setOnAction(e -> {
            hostServices.showDocument(UITextConstant.GITEE_URL);
        });
        alert.getDialogPane().setContent(new VBox(about, hyperlink));
        alert.showAndWait();
    }

    /**
     * 刷新网卡驱动
     */
    @FXML
    protected void onDriveRefreshButtonClick() {
        ObservableList<String> items = networkDrive.getItems();

        //现检查有没有wpcap驱动
        if (!pcapTool.checkWpcapDll()) {
            statusBar.setText(UITextConstant.NOT_FOUND_WPCAP);
            this.messageUrlAlert(
                    UITextConstant.ALERT_WARNING,
                    UITextConstant.NOT_FOUND_WPCAP + UITextConstant.NOT_FOUND_WPCAP1 + UITextConstant.NOT_FOUND_WPCAP2 + UITextConstant.NOT_FOUND_WPCAP3,
                    UITextConstant.WPCAP_URL,
                    AlertType.WARNING);
            return;
        }

        //获取网卡驱动
        List<PcapNetworkInterface> allDrive = pcapTool.getCaptureNetworkInterface();
        for (PcapNetworkInterface drive : allDrive) {
            if (drive.isLoopBack()) continue;

            // 获取联网IP地址
            List<PcapAddress> addresses = drive.getAddresses();
            for (PcapAddress pcapAddress : addresses) {
                //添加进networkDrive
                String ip = pcapAddress.getAddress().toString();
                items.add(ip);

                //选择一个默认的
                if (ip.contains(PcapConstant.DEFAULT_IP_TAG)) {
                    driveMap.put(ip.hashCode(), drive);
                    networkDrive.setValue(ip);
                }
            }
        }
        statusBar.setText(UITextConstant.DRIVE_REFRESH_SUCCESS);
    }

    /**
     * 开始抓包
     */
    @FXML
    protected void onStartCapture() {
        if (startCaptureBtn.getText().equals("开始捕获")) {
            //开始抓包
            PcapNetworkInterface nif = driveMap.get(networkDrive.getValue().hashCode());
            pcapTool.setCaptureTag(true);
            new Thread(() -> {
                pcapTool.capture(nif, PcapConstant.FILTER, PcapConstant.TIMEOUT);
                latch.countDown();
            }).start();

            //更新ui
            //todo:这里超时时间需要封装出来
            statusBar.setText("开始捕获->等待登录游戏");
            startCaptureBtn.setText("停止捕获");
        } else if (startCaptureBtn.getText().equals("停止捕获")) {
            //停止
            pcapTool.setCaptureTag(false);
            //更新ui
            statusBar.setText("停止捕获->正在处理数据");
            startCaptureBtn.setDisable(true);
            startCaptureBtn.setText("开始捕获");

            //等待数据
            new Thread(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                //处理数据
                List<String> finalBuffer = pcapTool.getFinalBuffer();
                //请求items
                mainService.requestItems(finalBuffer);
                //解析数据,获得等待导出的Map/json字符串
                try {
                    mainService.converterItems();
                    Platform.runLater(() -> {
                        //更新ui
                        startCaptureBtn.setDisable(false);
                        statusBar.setText("数据处理完成，可以导出数据");
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        //更新ui
                        startCaptureBtn.setDisable(false);
                        statusBar.setText("数据处理异常，请重新操作。");
                    });
                }
            }).start();

        }
    }

    /**
     * 导出文件
     */
    @FXML
    protected void onExportFile() {
        String resultJson = mainService.getJsonFromResultMap();
        if (Objects.equals(resultJson, "")) {
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("gear");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("文本文件", "*.txt"));
        Stage stage = (Stage) exportFileBtn.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(resultJson);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        statusBar.setText("导出完成");
    }

    /**
     * 消息框
     */
    protected void messageAlert(String title, String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 消息框
     */
    protected void messageUrlAlert(String title, String message, String url, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        Hyperlink hyperlink = new Hyperlink(message);
        hyperlink.setOnAction(e -> {
            hostServices.showDocument(url);
        });
        alert.getDialogPane().setContent(new VBox(hyperlink));
        alert.showAndWait();
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    /**
     * 使用说明
     */
    public void onInstructionBook() {
        //对话框
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(UITextConstant.INSTRUCTION_BOOK);
        alert.setHeaderText(null);
        //对话框内容
        Label about = new Label();
        about.setText(UITextConstant.INSTRUCTION_BOOK1);
        //超链接
        Hyperlink hyperlink = new Hyperlink("怎么查看本机ip");
        hyperlink.setOnAction(e -> {
            hostServices.showDocument(UITextConstant.HOW_GET_IPV4);
        });
        alert.getDialogPane().setContent(new VBox(about, hyperlink));
        //超链接
        alert.showAndWait();
    }
}