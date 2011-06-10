package test.zeromq
/**
 * User: Reuben Morais
 * Date: 10/06/11
 * Time: 08:31
 */

import org.zeromq.*;

class Player extends ZMQActor {
    String name
    int myNum

    public Player(ZMQ.Context ctx, String addr, String name) {
        super(ctx, ZMQ.REQ)
        this.name = name
        connect(addr)
    }

    void act() {
        loop {
            myNum = new Random().nextInt(10)
            send myNum
            react {
                switch (it) {
                    case 'too large':
                        println "$name: $myNum was too large"
                        break
                    case 'too small':
                        println "$name: $myNum was too small"
                        break
                    case 'you win':
                        println "$name: I won $myNum"; terminate()
                }
            }
        }
    }
}

def ctx = ZMQ.context(1)
new Player(ctx, 'tcp://localhost:54321', 'Player').start()
