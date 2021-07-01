package server.handler;


import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringInputHandler extends ChannelInboundHandlerAdapter {

    Path pathServer = Path.of("server");  //путь к корневой дериктории

    private Map<Channel, String> clients = new HashMap<>();  //Коллекция с именами пользователей.

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        clients.putIfAbsent(ctx.channel(), "qwe");  //Добавляю нового пользоввателя.

        String message = String.valueOf(msg);
        String[] cmd = message
                .replace("\n", "")
                .replace("\r", "")
                .split(" ");

//                создание файла
        if ("touch".equals(cmd[0])) {
            ctx.write(createFile(cmd[1], pathServer));

//               создание директории
        } else if ("mkdir".equals(cmd[0])) {
            ctx.write(createDirectory(cmd[1], pathServer));

//                удаление
        }else if ("rm".equals(cmd[0])) {
            ctx.write(remove(cmd[1], pathServer));

//                копирование
        }else if ("copy".equals(cmd[0])) {
            ctx.write(copy(cmd[1], pathServer, cmd[2]));

//                чтение из файла
        } else if ("cat".equals(cmd[0])){
            ctx.write(readFile(cmd[1], pathServer));

//                смена директории
        } else if ("cd".equals(cmd[0])) {
            ctx.write(changeDirectory(cmd[1]));

//                смена имени пользователя
        } else if ("changenick".equals(cmd[0])){
            ctx.write(changeNick(cmd[1], ctx.channel()));

//            загрузка файла
        }
//        else if ("upload".equals(cmd[0])){
//            ctx.write(upload(cmd[1], int fileLength, byte[] buffer);
    }


    //Создание файла
    private String createFile(String fileName, Path pathServer) throws IOException {
        if(!Files.exists(Path.of(String.valueOf(pathServer), fileName))){
            Files.createFile(Path.of(String.valueOf(pathServer), fileName)) ;
            return "File " + fileName + " created.\n";
        }else {
            return "File " + fileName + " already exists\n";
        }
    }

    //Создание директории
    private String createDirectory(String dir, Path pathServer) throws IOException {
        if(!Files.exists(Path.of(String.valueOf(pathServer), dir))){
            Files.createDirectory(Path.of(String.valueOf(pathServer), dir));
            return "Directory " + dir + " created.\n";
        }else {
            return "Directory " + dir + " already exists\n";
        }
    }

    //Удаление. Добавил рекурсивное удаление папок с файлами.
    private String remove(String fileName, Path pathServer) throws IOException {
        Path pathRM = Path.of(pathServer + "/" + fileName);
        Files.walkFileTree(pathRM, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
        return "File/directory " + fileName + " deleted.\n";
    }

    //Копирование.
    //В дз по нио вы написали- "пользователь может находиться не только в корневой директории. будет ошибка".
    //Но pathServer меняется при смене директории(cd). Проверил, вроде все работает. Или я непонял где ошибка.
    private String copy(String fileName, Path pathServer, String target) throws IOException {
        Files.copy((Path.of(String.valueOf(pathServer), fileName)), (Path.of(String.valueOf(pathServer), target)));
        return "File/Directory " + fileName + " copy in " + target;
    }

    //Чтение из файла
    private String readFile(String fileName, Path pathServer) throws IOException {
        List<String> textFile = Files.readAllLines(Path.of(String.valueOf(pathServer), fileName), StandardCharsets.UTF_8);
        for (String s: textFile) {
            return s;
        }
        return "\n";
    }

    //    смена дирректории
    private String changeDirectory(String cd) {
        if ("~".equals(cd)){
            pathServer = Path.of("server");
        } else if ("..".equals(cd)){
            pathServer = pathServer.getParent();
        } else{
            pathServer = Path.of(String.valueOf(pathServer), cd);
        }
        return "change directory " + pathServer;
    }

    //    смена имени пользователя
    private String changeNick(String name, Channel client) {
        clients.replaceAll((k, v) -> name);
        return "New nickname " + name;
    }

    //загрузка файлов
//    private String upload(String fileName, )
}